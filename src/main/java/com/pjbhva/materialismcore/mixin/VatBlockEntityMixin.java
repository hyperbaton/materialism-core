package com.pjbhva.materialismcore.mixin;

import com.drmangotea.tfmg.content.electricity.base.ElectricBlockEntity;
import com.drmangotea.tfmg.content.machinery.vat.base.IVatMachine;
import com.drmangotea.tfmg.content.machinery.vat.base.VatBlockEntity;
import com.drmangotea.tfmg.content.machinery.vat.base.registry.operations.VatOperation;
import com.drmangotea.tfmg.content.machinery.vat.electrode_holder.ElectrodeHolderBlockEntity;
import com.drmangotea.tfmg.recipes.VatMachineRecipe;
import com.pjbhva.materialismcore.compat.VatHeatReceiver;
import com.pjbhva.materialismcore.compat.VatRecipeExtras;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.foundation.utility.CreateLang;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.heat.IHeatConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Applies the requirements in {@link VatRecipeExtras}.
 * <p>
 * Temperature works like a TFC crucible: heat sources under the vat push their temperature into it
 * through {@link IHeatConsumer}. The vat heats towards the average across its whole footprint, so
 * a wide vat needs a heater under every column. Create-style heaters have no real temperature and
 * count as {@link #HEATED_TEMPERATURE} or {@link #SUPERHEATED_TEMPERATURE}.
 * <p>
 * TFMG's electrical network only models instantaneous power, and electrodes and freezers draw it constantly.
 * Energy is metered here instead: each tick adds what the recipe's machines are drawing, and the recipe
 * can't finish until it has reached the recipe's total, on top of its usual processing time.
 */
@Mixin(VatBlockEntity.class)
public abstract class VatBlockEntityMixin implements IHeatConsumer, VatHeatReceiver {
    @Unique
    private static final float HEATED_TEMPERATURE = 600f;
    @Unique
    private static final float SUPERHEATED_TEMPERATURE = 1200f;
    // TFC heat sources push every tick, and the target is only recalculated on lazy ticks.
    @Unique
    private static final int HEAT_MEMORY_TICKS = 20;

    @Shadow
    public VatMachineRecipe recipe;
    @Shadow
    public Map<BlockPos, VatOperation> machineMap;
    @Shadow
    int timer;
    @Shadow
    protected int width;

    @Shadow
    public abstract boolean isController();

    @Unique
    private float materialismcore$temperature;
    @Unique
    private float materialismcore$targetTemperature;
    @Unique
    private float materialismcore$receivedHeat;
    @Unique
    private long materialismcore$receivedHeatTime;
    @Unique
    private long materialismcore$energyDrawn;
    @Unique
    private VatMachineRecipe materialismcore$meteredRecipe;
    // The recipe handleRecipe() started with; result output can clear the field before it finishes.
    @Unique
    private VatMachineRecipe materialismcore$handledRecipe;

    @Override
    public float getTemperature() {
        VatBlockEntity self = (VatBlockEntity) (Object) this;
        VatBlockEntity controller = self.getControllerBE();
        return controller == null || controller == self
                ? materialismcore$temperature
                : ((IHeatConsumer) (Object) controller).getTemperature();
    }

    @Override
    public void setTemperature(float temperature) {
        Level level = ((VatBlockEntity) (Object) this).getLevel();
        materialismcore$receivedHeat = temperature;
        materialismcore$receivedHeatTime = level == null ? 0 : level.getGameTime();
    }

    @Override
    public float materialismcore$recentHeat(long gameTime) {
        return gameTime - materialismcore$receivedHeatTime <= HEAT_MEMORY_TICKS ? materialismcore$receivedHeat : 0f;
    }

    @Inject(method = "updateTemperature()V", at = @At("TAIL"))
    private void materialismcore$updateTargetTemperature(CallbackInfo ci) {
        VatBlockEntity self = (VatBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null || level.isClientSide || !isController() || width <= 0) {
            return;
        }

        float total = 0f;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                BlockPos pos = self.getBlockPos().offset(x, 0, z);
                float pushed = level.getBlockEntity(pos) instanceof VatHeatReceiver receiver
                        ? receiver.materialismcore$recentHeat(level.getGameTime())
                        : 0f;
                float heatLevel = BoilerHeater.findHeat(level, pos.below(), level.getBlockState(pos.below()));
                float heater = heatLevel >= 2f ? SUPERHEATED_TEMPERATURE : heatLevel >= 1f ? HEATED_TEMPERATURE : 0f;
                total += Math.max(pushed, heater);
            }
        }
        materialismcore$targetTemperature = total / (width * width);
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void materialismcore$adjustTemperature(CallbackInfo ci) {
        Level level = ((VatBlockEntity) (Object) this).getLevel();
        if (level != null && !level.isClientSide && isController()) {
            materialismcore$temperature = HeatCapability.adjustTempTowards(materialismcore$temperature, materialismcore$targetTemperature);
        }
    }

    @Inject(method = "handleRecipe()V", at = @At("HEAD"), cancellable = true)
    private void materialismcore$checkRequirements(CallbackInfo ci) {
        materialismcore$handledRecipe = recipe;
        VatBlockEntity self = (VatBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null || level.isClientSide || recipe == null || !isController()) {
            return;
        }

        VatRecipeExtras extras = VatRecipeExtras.of(recipe);
        if (materialismcore$temperature < extras.temperature()) {
            ci.cancel();
            return;
        }
        if (extras.minCurrent() <= 0 && extras.energy() <= 0) {
            return;
        }

        List<ElectricBlockEntity> machines = materialismcore$recipeElectricMachines(level, recipe);
        if (machines.isEmpty()) {
            return;
        }
        for (ElectricBlockEntity machine : machines) {
            if (machine.getCurrent() < extras.minCurrent()
                    || machine instanceof ElectrodeHolderBlockEntity electrode && !electrode.canWork()) {
                ci.cancel();
                return;
            }
        }

        if (extras.energy() > 0) {
            if (materialismcore$meteredRecipe != recipe) {
                materialismcore$meteredRecipe = recipe;
                materialismcore$energyDrawn = 0;
            }
            if (materialismcore$energyDrawn < extras.energy()) {
                for (ElectricBlockEntity machine : machines) {
                    if (((IVatMachine) machine).canOperate(self)) {
                        materialismcore$energyDrawn += (long) machine.getPowerUsage();
                    }
                }
            }
            if (materialismcore$energyDrawn < extras.energy() && timer >= recipe.getProcessingDuration()) {
                ci.cancel();
            }
        }
    }

    // handleRecipe() only clears the recipe once it has produced its results.
    @Inject(
            method = "handleRecipe()V",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/drmangotea/tfmg/content/machinery/vat/base/VatBlockEntity;recipe:Lcom/drmangotea/tfmg/recipes/VatMachineRecipe;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void materialismcore$onRecipeFinished(CallbackInfo ci) {
        materialismcore$energyDrawn = 0;
        materialismcore$meteredRecipe = null;

        VatMachineRecipe finished = materialismcore$handledRecipe;
        materialismcore$handledRecipe = null;
        if (finished == null) {
            return;
        }
        int wear = VatRecipeExtras.of(finished).electrodeWear();
        if (wear > 0 && ((VatBlockEntity) (Object) this).getLevel() instanceof ServerLevel level) {
            for (ElectricBlockEntity machine : materialismcore$recipeElectricMachines(level, finished)) {
                if (!(machine instanceof ElectrodeHolderBlockEntity electrode)) {
                    continue;
                }
                ItemStack stack = electrode.inventory.getStackInSlot(0).copy();
                if (stack.isDamageableItem()) {
                    stack.hurtAndBreak(wear, level, null, item ->
                            level.playSound(null, electrode.getBlockPos(), SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1f, 1f));
                    electrode.inventory.setStackInSlot(0, stack);
                }
            }
        }
    }

    /** The powered machines (electrodes, freezers) fulfilling the recipe, leaving out any spare ones on the vat. */
    @Unique
    private List<ElectricBlockEntity> materialismcore$recipeElectricMachines(Level level, VatMachineRecipe forRecipe) {
        List<VatOperation> required = new ArrayList<>(forRecipe.machines);
        List<ElectricBlockEntity> machines = new ArrayList<>();
        for (Map.Entry<BlockPos, VatOperation> machine : machineMap.entrySet()) {
            if (required.remove(machine.getValue())
                    && level.getBlockEntity(machine.getKey()) instanceof ElectricBlockEntity electric
                    && electric instanceof IVatMachine) {
                machines.add(electric);
            }
        }
        return machines;
    }

    @Inject(
            method = "addToGoggleTooltip(Ljava/util/List;Z)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/drmangotea/tfmg/base/lang/TFMGTexts$Vat;attachments()Lnet/createmod/catnip/lang/LangBuilder;"
            )
    )
    private void materialismcore$addTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
        CreateLang.builder()
                .add(Component.translatable("materialismcore.vat.temperature", Math.round(materialismcore$temperature)))
                .forGoggles(tooltip, 1);
        if (recipe != null && VatRecipeExtras.of(recipe).energy() > 0) {
            CreateLang.builder()
                    .add(Component.translatable("materialismcore.vat.energy", materialismcore$energyDrawn, VatRecipeExtras.of(recipe).energy()))
                    .forGoggles(tooltip, 1);
        }
    }

    @Inject(method = "write(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Z)V", at = @At("HEAD"))
    private void materialismcore$write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        compound.putFloat("MaterialismTemperature", materialismcore$temperature);
        compound.putLong("MaterialismEnergyDrawn", materialismcore$energyDrawn);
    }

    @Inject(method = "read(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Z)V", at = @At("TAIL"))
    private void materialismcore$read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        materialismcore$temperature = compound.getFloat("MaterialismTemperature");
        materialismcore$energyDrawn = compound.getLong("MaterialismEnergyDrawn");
    }
}
