package com.pjbhva.materialismcore.mixin;

import com.drmangotea.tfmg.content.machinery.metallurgy.casting_basin.CastingBasinBlockEntity;
import com.drmangotea.tfmg.recipes.CastingRecipe;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Two fixes for TFMG's casting basin, both driven by the same underlying issue: it treats its
 * fixed 144mB tank capacity as the recipe cost, instead of the recipe's own declared amount.
 */
@Mixin(CastingBasinBlockEntity.class)
public abstract class CastingBasinBlockEntityMixin {

    @Shadow
    public CastingRecipe recipe;

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/fluids/capability/templates/FluidTank;getSpace()I"
            )
    )
    private int materialismcore$dontRequireFullTank(FluidTank tank) {
        return 0;
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/fluids/capability/templates/FluidTank;setFluid(Lnet/neoforged/neoforge/fluids/FluidStack;)V"
            )
    )
    private void materialismcore$drainOnlyRecipeAmount(FluidTank tank, FluidStack ignoredEmptyStack) {
        FluidStack current = tank.getFluid();
        int recipeAmount = this.recipe != null ? this.recipe.getIngrenient().amount() : current.getAmount();
        int remaining = current.getAmount() - recipeAmount;

        if (remaining > 0) {
            FluidStack leftover = current.copy();
            leftover.setAmount(remaining);
            tank.setFluid(leftover);
        } else {
            tank.setFluid(FluidStack.EMPTY);
        }
    }
}
