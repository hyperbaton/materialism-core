package com.pjbhva.materialismcore.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.jei.category.SequencedAssemblyCategory;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

/**
 * Create's JEI page for sequenced assembly lays the steps out in a single row, 25px wide each with
 * a fixed 3px gap, inside a 180px-wide category. That fits six steps; from the seventh onwards the
 * row overflows the recipe frame on both sides, and the step numerals stop at "VI" (every later
 * step is labelled "-").
 * <p>
 * Whenever the natural row doesn't fit, this shrinks the gap between steps (going negative if
 * needed, so the 25px columns overlap a bit while the 18px slots stay apart) until the row fits
 * the frame, and scales each step's animated machine down by the same ratio so the machines don't
 * pile on top of each other. The same gap is used for slot placement, drawing and tooltip hit
 * tests, so they stay aligned. Numerals are generated for any step count.
 */
@Mixin(SequencedAssemblyCategory.class)
public abstract class SequencedAssemblyCategoryMixin {

    private static final String SET_RECIPE = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/simibubi/create/content/processing/sequenced/SequencedAssemblyRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V";
    private static final String DRAW = "draw(Lcom/simibubi/create/content/processing/sequenced/SequencedAssemblyRecipe;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/client/gui/GuiGraphics;DD)V";
    private static final String GET_TOOLTIP_STRINGS = "getTooltipStrings(Lcom/simibubi/create/content/processing/sequenced/SequencedAssemblyRecipe;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;DD)Ljava/util/List;";

    private static final int DEFAULT_MARGIN = 3;
    // Keeps the outermost steps from touching the frame edge once the row has been compressed.
    private static final int SIDE_PADDING = 2;
    // Height (in category space) the step animations are drawn around; scaling about it keeps
    // the shrunk machines vertically where they were instead of drifting towards the top.
    private static final float ANIMATION_PIVOT_Y = 50f;

    private static final int[] ROMAN_VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] ROMAN_SYMBOLS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    @Shadow
    private SequencedAssemblySubCategory getSubCategory(SequencedRecipe<?> sequencedRecipe) {
        throw new AssertionError();
    }

    @ModifyExpressionValue(
            method = {SET_RECIPE, DRAW, GET_TOOLTIP_STRINGS},
            at = @At(value = "CONSTANT", args = "intValue=3")
    )
    private int materialismcore$fitStepMargin(int margin, @Local(argsOnly = true) SequencedAssemblyRecipe recipe) {
        return materialismcore$stepMargin(recipe);
    }

    // Create looks the numeral up in a fixed array clamped to "-" past the sixth step; build it from
    // the step index instead (third Component.literal in draw, after the "?" and "x<loops>" labels).
    @ModifyArg(
            method = DRAW,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;",
                    ordinal = 2
            )
    )
    private String materialismcore$stepNumeral(String numeral, @Local(name = "i") int index) {
        return materialismcore$toRoman(index + 1);
    }

    @WrapOperation(
            method = DRAW,
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/compat/jei/category/sequencedAssembly/SequencedAssemblySubCategory;draw(Lcom/simibubi/create/content/processing/sequenced/SequencedRecipe;Lnet/minecraft/client/gui/GuiGraphics;DDI)V"
            )
    )
    private void materialismcore$scaleStepAnimation(SequencedAssemblySubCategory subCategory, SequencedRecipe<?> step,
                                                     GuiGraphics graphics, double mouseX, double mouseY, int index,
                                                     Operation<Void> original,
                                                     @Local(argsOnly = true) SequencedAssemblyRecipe recipe) {
        int margin = materialismcore$stepMargin(recipe);
        if (margin >= DEFAULT_MARGIN) {
            original.call(subCategory, step, graphics, mouseX, mouseY, index);
            return;
        }

        int width = subCategory.getWidth();
        float scale = (float) (width + margin) / (width + DEFAULT_MARGIN);
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(width / 2f, ANIMATION_PIVOT_Y, 0f);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(width / -2f, -ANIMATION_PIVOT_Y, 0f);
        original.call(subCategory, step, graphics, mouseX, mouseY, index);
        poseStack.popPose();
    }

    private int materialismcore$stepMargin(SequencedAssemblyRecipe recipe) {
        List<SequencedRecipe<?>> sequence = recipe.getSequence();
        int gaps = sequence.size() - 1;
        if (gaps <= 0) {
            return DEFAULT_MARGIN;
        }

        int stepsWidth = 0;
        for (SequencedRecipe<?> step : sequence) {
            stepsWidth += getSubCategory(step).getWidth();
        }

        int available = ((SequencedAssemblyCategory) (Object) this).getBackground().getWidth();
        if (stepsWidth + gaps * DEFAULT_MARGIN <= available) {
            return DEFAULT_MARGIN;
        }
        return Math.floorDiv(available - 2 * SIDE_PADDING - stepsWidth, gaps);
    }

    private static String materialismcore$toRoman(int number) {
        StringBuilder numeral = new StringBuilder();
        for (int i = 0; i < ROMAN_VALUES.length; i++) {
            while (number >= ROMAN_VALUES[i]) {
                numeral.append(ROMAN_SYMBOLS[i]);
                number -= ROMAN_VALUES[i];
            }
        }
        return numeral.toString();
    }
}
