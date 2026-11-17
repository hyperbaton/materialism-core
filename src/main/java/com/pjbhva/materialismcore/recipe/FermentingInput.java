package com.pjbhva.materialismcore.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

/** What sits in a fermentation vat: its item slot and its input tank. */
public record FermentingInput(ItemStack item, FluidStack fluid) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return item;
    }

    @Override
    public int size() {
        return 1;
    }
}
