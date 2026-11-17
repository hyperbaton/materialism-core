package com.pjbhva.materialismcore.mixin.solarpunk;

import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// The vat's output tank holds whatever a fermenting recipe makes; only the vat itself fills it
@Mixin(targets = "net.succ.solar_punk.block.entity.custom.FermentationVatBlockEntity$2", remap = false)
public abstract class FermentationVatOutputTankMixin {
    @Inject(method = "isFluidValid", at = @At("HEAD"), cancellable = true)
    private void materialismcore$anyResult(FluidStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
