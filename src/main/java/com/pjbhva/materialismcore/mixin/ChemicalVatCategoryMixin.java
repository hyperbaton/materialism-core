package com.pjbhva.materialismcore.mixin;

import com.drmangotea.tfmg.integration.jei.category.ChemicalVatCategory;
import com.drmangotea.tfmg.recipes.VatMachineRecipe;
import com.pjbhva.materialismcore.compat.VatRecipeExtras;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Shows the fields in {@link VatRecipeExtras} on TFMG's JEI page: a compact summary in the strip
 * where the heat icons go, and the full description when hovering that strip.
 */
@Mixin(ChemicalVatCategory.class)
public abstract class ChemicalVatCategoryMixin {
    private static final String DRAW = "draw(Lcom/drmangotea/tfmg/recipes/VatMachineRecipe;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/client/gui/GuiGraphics;DD)V";
    private static final String GET_TOOLTIP_STRINGS = "getTooltipStrings(Lcom/drmangotea/tfmg/recipes/VatMachineRecipe;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;DD)Ljava/util/List;";

    private static final int STRIP_LEFT = 10;
    // The result slots start here.
    private static final int STRIP_RIGHT = 125;
    private static final int STRIP_TOP = 109;
    private static final int STRIP_BOTTOM = 118;
    private static final int MAX_HEAT_ICONS = 9;
    private static final float MAX_SCALE = 0.75f;
    private static final int TEXT_COLOR = 0xFCFCFC;

    @Inject(method = DRAW, at = @At("TAIL"))
    private void materialismcore$drawRequirements(VatMachineRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                                                  double mouseX, double mouseY, CallbackInfo ci) {
        List<Component> requirements = VatRecipeExtras.of(recipe).requirements();
        if (requirements.isEmpty()) {
            return;
        }

        MutableComponent summary = Component.empty();
        for (int i = 0; i < requirements.size(); i++) {
            summary.append(i == 0 ? "" : "  ").append(requirements.get(i));
        }

        Font font = Minecraft.getInstance().font;
        int heatIcons = Math.min(Math.abs(recipe.heatLevel), MAX_HEAT_ICONS);
        int left = heatIcons == 0 ? STRIP_LEFT : (heatIcons + 1) * 10 + 2;
        float scale = Math.min(MAX_SCALE, (float) (STRIP_RIGHT - left) / font.width(summary));

        graphics.pose().pushPose();
        graphics.pose().translate(left, STRIP_TOP + 2, 0);
        graphics.pose().scale(scale, scale, 1f);
        graphics.drawString(font, summary, 0, 0, TEXT_COLOR);
        graphics.pose().popPose();
    }

    @Inject(method = GET_TOOLTIP_STRINGS, at = @At("RETURN"))
    private void materialismcore$addTooltip(VatMachineRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY,
                                            CallbackInfoReturnable<List<Component>> cir) {
        if (mouseX > STRIP_LEFT && mouseX < STRIP_RIGHT && mouseY > STRIP_TOP && mouseY < STRIP_BOTTOM) {
            cir.getReturnValue().addAll(VatRecipeExtras.of(recipe).tooltip());
        }
    }
}
