package com.pjbhva.materialismcore.block;

import com.pjbhva.materialismcore.registry.ModBlockEntities;
import com.pjbhva.materialismcore.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class GalvanicCellBlockEntity extends BlockEntity {
    private int energy;

    public GalvanicCellBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GALVANIC_CELL.get(), pos, state);
        this.energy = GalvanicCellBlock.MAX_ENERGY;
    }

    public int getStoredEnergy() {
        return energy;
    }

    public void setStoredEnergy(int energy) {
        this.energy = Math.max(0, Math.min(energy, GalvanicCellBlock.MAX_ENERGY));
        setChanged();
    }

    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = Math.min(energy, Math.min(maxExtract, GalvanicCellBlock.MAX_EXTRACT));
        if (!simulate) {
            energy -= extracted;
            setChanged();
        }
        return extracted;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GalvanicCellBlockEntity be) {
        if (be.energy <= 0) return;

        for (Direction dir : Direction.values()) {
            if (be.energy <= 0) break;

            IEnergyStorage receiver = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos.relative(dir), dir.getOpposite());
            if (receiver != null && receiver.canReceive()) {
                int toSend = Math.min(be.energy, GalvanicCellBlock.MAX_EXTRACT);
                int accepted = receiver.receiveEnergy(toSend, false);
                if (accepted > 0) {
                    be.energy -= accepted;
                    be.setChanged();
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", energy);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getInt("Energy");
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        Integer stored = componentInput.get(ModDataComponents.STORED_ENERGY.get());
        if (stored != null) {
            this.energy = stored;
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(ModDataComponents.STORED_ENERGY.get(), this.energy);
    }
}
