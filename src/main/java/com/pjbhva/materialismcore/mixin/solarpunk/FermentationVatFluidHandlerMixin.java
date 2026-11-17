package com.pjbhva.materialismcore.mixin.solarpunk;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.pjbhva.materialismcore.compat.Fermenting;
import net.minecraft.world.level.material.Fluid;
import net.succ.solar_punk.block.entity.custom.FermentationVatBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

// Pipes may fill the vat with any fermenting recipe fluid, not just water
@Mixin(targets = "net.succ.solar_punk.block.entity.custom.FermentationVatBlockEntity$4", remap = false)
public abstract class FermentationVatFluidHandlerMixin {
    @Shadow
    @Final
    FermentationVatBlockEntity this$0;

    @WrapOperation(method = {"isFluidValid", "fill"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/material/Fluid;isSame(Lnet/minecraft/world/level/material/Fluid;)Z"))
    private boolean materialismcore$recipeFluid(Fluid fluid, Fluid water, Operation<Boolean> original) {
        return Fermenting.isFluidInput(this$0.getLevel(), fluid);
    }
}
