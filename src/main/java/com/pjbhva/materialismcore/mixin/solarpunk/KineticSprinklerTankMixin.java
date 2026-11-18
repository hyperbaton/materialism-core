package com.pjbhva.materialismcore.mixin.solarpunk;

import com.pjbhva.materialismcore.compat.Sprinklers;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// The sprinkler tank also takes herbicides
@Mixin(targets = "net.succ.solar_punk.block.entity.custom.KineticSprinklerBlockEntity$1", remap = false)
public abstract class KineticSprinklerTankMixin {
    @Inject(method = "isFluidValid", at = @At("HEAD"), cancellable = true)
    private void materialismcore$herbicides(FluidStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(Sprinklers.HERBICIDES)) {
            cir.setReturnValue(true);
        }
    }
}
