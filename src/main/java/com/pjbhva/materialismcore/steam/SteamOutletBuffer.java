package com.pjbhva.materialismcore.steam;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

// Pressurized steam waiting in a steam engine used as an outlet
public class SteamOutletBuffer implements INBTSerializable<CompoundTag> {
    public static final int CAPACITY = 1000;

    private int amount;
    private float partial;

    public int amount() {
        return amount;
    }

    public boolean add(float mb) {
        if (amount >= CAPACITY) return false;
        partial += mb;
        int whole = (int) partial;
        if (whole <= 0) return false;
        partial -= whole;
        amount = Math.min(CAPACITY, amount + whole);
        return true;
    }

    public int drain(int max, boolean execute) {
        int drained = Math.min(max, amount);
        if (execute) amount -= drained;
        return drained;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Amount", amount);
        tag.putFloat("Partial", partial);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        amount = tag.getInt("Amount");
        partial = tag.getFloat("Partial");
    }
}
