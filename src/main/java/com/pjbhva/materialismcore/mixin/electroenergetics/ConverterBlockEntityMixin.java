package com.pjbhva.materialismcore.mixin.electroenergetics;

import com.drmangotea.tfmg.content.electricity.base.ElectricBlockValues;
import com.drmangotea.tfmg.content.electricity.base.IElectric;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.converter.ConverterBlock;
import com.george_vi.electroenergetics.content.converter.ConverterBlockEntity;
import com.george_vi.electroenergetics.content.converter.ConverterDevice;
import com.pjbhva.materialismcore.ModConfigs;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Joins the EE converter to the TFMG network on its facing side: a generator paid from the EE buffer in
 * output mode, a load that fills the EE buffer in input mode.
 */
@Mixin(value = ConverterBlockEntity.class, remap = false)
public abstract class ConverterBlockEntityMixin extends SmartBlockEntity implements IElectric {
    // Buffer fraction needed before a drained converter resumes supplying.
    @Unique
    private static final double RESUME_FRACTION = 0.5;
    @Unique
    private static final int LAZY_TICKS = 10;

    @Shadow
    ConverterDevice converterDevice;
    @Shadow
    double power;
    @Shadow
    protected ScrollValueBehaviour voltage;

    @Unique
    private ElectricBlockValues materialism$tfmgData;
    @Unique
    private boolean materialism$supplying;
    @Unique
    private float materialism$draw;
    @Unique
    private int materialism$lazyCounter;

    private ConverterBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void materialism$initTfmg(BlockEntityType<?> type, BlockPos pos, BlockState state, CallbackInfo ci) {
        materialism$tfmgData = new ElectricBlockValues(pos);
        materialism$tfmgData.connectNextTick = true;
    }

    @Override
    public ElectricBlockValues getData() {
        if (materialism$tfmgData == null) {
            materialism$tfmgData = new ElectricBlockValues(getBlockPos());
        }
        return materialism$tfmgData;
    }

    @Override
    public BlockPos getPos() {
        return getBlockPos();
    }

    @Override
    public LevelAccessor getLevelAccessor() {
        return level;
    }

    @Override
    public void sendStuff() {
        sendData();
    }

    @Override
    public boolean hasElectricitySlot(Direction direction) {
        return direction == materialism$facedDirection();
    }

    @Override
    public int getMaxVoltage() {
        return 100000;
    }

    @Override
    public int voltageGeneration() {
        return materialism$supplying && !materialism$isSource() ? materialism$tfmgVoltage() : 0;
    }

    @Override
    public float powerGeneration() {
        return materialism$supplying && !materialism$isSource() ? (float) materialism$maxTfmgPower() : 0f;
    }

    @Override
    public float resistance() {
        int volts = getData().getVoltage();
        if (!materialism$isSource() || materialism$draw <= 0f || volts <= 0) {
            return 0f;
        }
        return (float) volts * volts / materialism$draw;
    }

    @Override
    public void remove() {
        super.remove();
        onRemoved();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void materialism$tickTfmg(CallbackInfo ci) {
        if (level == null) {
            return;
        }
        tickElectricity();
        boolean lazy = ++materialism$lazyCounter >= LAZY_TICKS;
        if (lazy) {
            materialism$lazyCounter = 0;
            lazyTickElectricity();
        }
        if (level.isClientSide || converterDevice == null || !converterDevice.isValid()) {
            return;
        }
        if (materialism$isSource()) {
            materialism$tickInput(lazy);
        } else {
            materialism$tickOutput();
        }
    }

    @Inject(method = "read", at = @At("TAIL"))
    private void materialism$readTfmg(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        readElectricity(tag, clientPacket);
        materialism$supplying = tag.getBoolean("MaterialismTfmgSupplying");
        materialism$draw = tag.getFloat("MaterialismTfmgDraw");
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void materialism$writeTfmg(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        tag.putBoolean("MaterialismTfmgSupplying", materialism$supplying);
        tag.putFloat("MaterialismTfmgDraw", materialism$draw);
    }

    // Pays this converter's share of the TFMG load from the EE buffer, stopping when it runs dry.
    @Unique
    private void materialism$tickOutput() {
        if (materialism$draw != 0f) {
            materialism$setDraw(0f);
        }
        double max = materialism$maxEnergy();
        if (!materialism$facesTfmg()) {
            materialism$setSupplying(false);
            return;
        }
        if (!materialism$supplying) {
            if (converterDevice.storedEnergy >= max * RESUME_FRACTION) {
                materialism$setSupplying(true);
            }
            return;
        }
        ElectricBlockValues data = getData();
        double cost = 0;
        if (data.getVoltage() > 0 && !data.notEnoughPower && data.networkPowerGeneration > 0) {
            double share = powerGeneration() / data.networkPowerGeneration;
            cost = Math.min(1.0, share) * getNetworkPowerUsage() * ModConfigs.EE_WATTS_PER_TFMG_WATT.get();
        }
        if (converterDevice.storedEnergy < cost) {
            materialism$setSupplying(false);
        } else {
            converterDevice.storedEnergy -= cost;
        }
    }

    // Draws from TFMG what the EE side is delivering, plus a refill of the buffer over a second.
    @Unique
    private void materialism$tickInput(boolean lazy) {
        materialism$setSupplying(false);
        ElectricBlockValues data = getData();
        double rate = ModConfigs.EE_WATTS_PER_TFMG_WATT.get();
        double max = materialism$maxEnergy();
        if (materialism$draw > 0f && data.getVoltage() > 0 && !data.notEnoughPower) {
            converterDevice.storedEnergy = Math.min(max, converterDevice.storedEnergy + materialism$draw * rate);
        }
        if (!lazy) {
            return;
        }
        float target = 0f;
        if (materialism$facesTfmg() && data.getVoltage() > 0) {
            double wanted = (Math.abs(power) + (max - converterDevice.storedEnergy) / 20.0) / rate;
            double headroom = data.networkPowerGeneration - (getNetworkPowerUsage() - getPowerUsage());
            target = (float) Math.max(0.0, Math.min(Math.min(wanted, headroom), materialism$maxTfmgPower()));
        }
        if (Math.abs(target - materialism$draw) > Math.max(1f, materialism$draw * 0.1f) || (target == 0f && materialism$draw != 0f)) {
            materialism$setDraw(target);
        }
    }

    @Unique
    private void materialism$setSupplying(boolean supplying) {
        if (materialism$supplying != supplying) {
            materialism$supplying = supplying;
            updateNextTick();
            sendData();
        }
    }

    @Unique
    private void materialism$setDraw(float draw) {
        materialism$draw = draw;
        updateNextTick();
        sendData();
    }

    @Unique
    private boolean materialism$isSource() {
        BlockState state = getBlockState();
        return state.hasProperty(ConverterBlock.SOURCE) && state.getValue(ConverterBlock.SOURCE);
    }

    @Unique
    private Direction materialism$facedDirection() {
        BlockState state = getBlockState();
        return state.hasProperty(ConverterBlock.FACING) ? state.getValue(ConverterBlock.FACING).getOpposite() : null;
    }

    @Unique
    private boolean materialism$facesTfmg() {
        Direction dir = materialism$facedDirection();
        return dir != null && level != null
                && level.getBlockEntity(getBlockPos().relative(dir)) instanceof IElectric other
                && other.hasElectricitySlot(dir.getOpposite());
    }

    @Unique
    private int materialism$tfmgVoltage() {
        return voltage == null ? 0 : voltage.getValue() * 10;
    }

    @Unique
    private double materialism$maxEnergy() {
        try {
            return CEEConfigs.server().converterMaxPowerKw.get() * 1000.0;
        } catch (RuntimeException e) {
            return 0.0;
        }
    }

    @Unique
    private double materialism$maxTfmgPower() {
        return materialism$maxEnergy() / ModConfigs.EE_WATTS_PER_TFMG_WATT.get();
    }
}
