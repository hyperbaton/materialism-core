package com.pjbhva.materialismcore.compat;

import com.pjbhva.materialismcore.recipe.FermentingInput;
import com.pjbhva.materialismcore.recipe.FermentingRecipe;
import com.pjbhva.materialismcore.registry.ModRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.succ.solar_punk.Config;

import java.util.List;

/** Runs {@code materialismcore:fermenting} recipes in the Solarpunk fermentation vat. */
public final class Fermenting {
    private Fermenting() {}

    private static List<RecipeHolder<FermentingRecipe>> recipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.FERMENTING.get());
    }

    public static FermentingRecipe find(Level level, ItemStack item, FluidStack fluid) {
        return level.getRecipeManager().getRecipeFor(ModRecipes.FERMENTING.get(), new FermentingInput(item, fluid), level)
                .map(RecipeHolder::value).orElse(null);
    }

    public static boolean isItemInput(Level level, ItemStack stack) {
        return level == null || recipes(level).stream().anyMatch(holder ->
                holder.value().ingredient().map(sized -> sized.ingredient().test(stack)).orElse(false));
    }

    public static boolean isFluidInput(Level level, Fluid fluid) {
        FluidStack stack = new FluidStack(fluid, 1);
        return level == null || recipes(level).stream().anyMatch(holder ->
                holder.value().fluidIngredient().ingredient().test(stack));
    }

    /** Batches run in parallel: one per minimum-vat volume. */
    public static int batches(int width, int height) {
        int minimum = Config.fermentationVatMinWidth * Config.fermentationVatMinWidth * Config.fermentationVatMinHeight;
        return Math.max(1, width * width * height / Math.max(1, minimum));
    }

    public static boolean canRun(FermentingRecipe recipe, int batches, ItemStackHandler items, FluidTank input, FluidTank output) {
        if (recipe.ingredient().isPresent() && items.getStackInSlot(0).getCount() < recipe.ingredient().get().count() * batches) {
            return false;
        }
        if (input.getFluidAmount() < recipe.fluidIngredient().amount() * batches) {
            return false;
        }
        FluidStack result = recipe.result().copyWithAmount(recipe.result().getAmount() * batches);
        return output.fill(result, IFluidHandler.FluidAction.SIMULATE) == result.getAmount();
    }

    public static void run(FermentingRecipe recipe, int batches, ItemStackHandler items, FluidTank input, FluidTank output) {
        recipe.ingredient().ifPresent(sized -> items.extractItem(0, sized.count() * batches, false));
        input.drain(recipe.fluidIngredient().amount() * batches, IFluidHandler.FluidAction.EXECUTE);
        output.fill(recipe.result().copyWithAmount(recipe.result().getAmount() * batches), IFluidHandler.FluidAction.EXECUTE);
    }
}
