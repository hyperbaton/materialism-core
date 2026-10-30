package com.pjbhva.materialismcore.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.pjbhva.materialismcore.registry.ModTags;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * TFMG's exhaust and smokestack tanks (anonymous {@code SmartFluidTank} subclasses in each block
 * entity) only accept carbon dioxide, so there is no way to get rid of other waste gases such as
 * the pack's oxygen. Both blocks just drain whatever is in the tank, so widening the tank's
 * {@code isFluidValid} to the {@code materialismcore:exhaustable} fluid tag is enough for them to
 * vent anything in it. TFMG's own CO2 check is kept, so CO2 still works even if a datapack
 * replaces the tag.
 */
@Mixin(targets = {
        "com.drmangotea.tfmg.content.machinery.misc.exhaust.ExhaustBlockEntity$1",
        "com.drmangotea.tfmg.content.machinery.misc.smokestack.SmokestackBlockEntity$1"
})
public abstract class ExhaustTankMixin {
    @ModifyReturnValue(method = "isFluidValid(Lnet/neoforged/neoforge/fluids/FluidStack;)Z", at = @At("RETURN"))
    private boolean materialismcore$acceptExhaustable(boolean valid, @Local(argsOnly = true) FluidStack stack) {
        return valid || stack.is(ModTags.Fluids.EXHAUSTABLE);
    }
}
