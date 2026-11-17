package com.pjbhva.materialismcore.compat.jei;

import com.pjbhva.materialismcore.MaterialismCore;
import com.pjbhva.materialismcore.recipe.FermentingRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class FermentingCategory implements IRecipeCategory<RecipeHolder<FermentingRecipe>> {
    public static final RecipeType<RecipeHolder<FermentingRecipe>> TYPE =
            RecipeType.createRecipeHolderType(ResourceLocation.fromNamespaceAndPath(MaterialismCore.MOD_ID, "fermenting"));

    private final IDrawable icon;
    private final IDrawable arrow;

    public FermentingCategory(IGuiHelper guiHelper, ItemStack vat) {
        this.icon = guiHelper.createDrawableItemStack(vat);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<RecipeHolder<FermentingRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("materialismcore.jei.fermenting");
    }

    @Override
    public int getWidth() {
        return 120;
    }

    @Override
    public int getHeight() {
        return 40;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<FermentingRecipe> holder, IFocusGroup focuses) {
        FermentingRecipe recipe = holder.value();
        recipe.ingredient().ifPresent(sized -> builder.addSlot(RecipeIngredientRole.INPUT, 5, 5)
                .setStandardSlotBackground()
                .addIngredients(VanillaTypes.ITEM_STACK, List.of(sized.getItems())));
        builder.addSlot(RecipeIngredientRole.INPUT, 25, 5)
                .setStandardSlotBackground()
                .addIngredients(NeoForgeTypes.FLUID_STACK, List.of(recipe.fluidIngredient().getFluids()));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 5)
                .setOutputSlotBackground()
                .addIngredients(NeoForgeTypes.FLUID_STACK, List.of(recipe.result()));
    }

    @Override
    public void draw(RecipeHolder<FermentingRecipe> holder, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        arrow.draw(graphics, 57, 5);
        Component time = Component.translatable("materialismcore.jei.fermenting.time", holder.value().processingTime() / 20);
        graphics.drawString(Minecraft.getInstance().font, time, 5, 29, 0xFF808080, false);
    }
}
