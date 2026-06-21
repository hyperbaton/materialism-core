package com.pjbhva.materialismcore.block;

import net.neoforged.neoforge.energy.IEnergyStorage;

public class GalvanicCellEnergyStorage implements IEnergyStorage {
    private final GalvanicCellBlockEntity cell;

    public GalvanicCellEnergyStorage(GalvanicCellBlockEntity cell) {
        this.cell = cell;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return cell.extractEnergy(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored() {
        return cell.getStoredEnergy();
    }

    @Override
    public int getMaxEnergyStored() {
        return GalvanicCellBlock.MAX_ENERGY;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return false;
    }
}
