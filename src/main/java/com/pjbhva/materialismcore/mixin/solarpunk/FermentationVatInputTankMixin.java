package com.pjbhva.materialismcore.mixin.solarpunk;

import com.pjbhva.materialismcore.compat.Fermenting;
import net.neoforged.neoforge.fluids.FluidStack;
import net.succ.solar_punk.block.entity.custom.FermentationVatBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// The vat's input tank (formerly water only) takes any fermenting recipe fluid
@Mixin(targets = "net.succ.solar_punk.block.entity.custom.FermentationVatBlockEntity$1", remap = false)
public abstract class FermentationVatInputTankMixin {
    @Shadow
    @Final
    FermentationVatBlockEntity this$0;

    @Inject(method = "isFluidValid", at = @At("HEAD"), cancellable = true)
    private void materialismcore$recipeFluids(FluidStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(Fermenting.isFluidInput(this$0.getLevel(), stack.getFluid()));
    }
}
