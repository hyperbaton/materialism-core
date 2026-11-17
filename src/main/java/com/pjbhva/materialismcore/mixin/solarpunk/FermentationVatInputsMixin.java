package com.pjbhva.materialismcore.mixin.solarpunk;

import com.pjbhva.materialismcore.compat.Fermenting;
import net.minecraft.world.item.ItemStack;
import net.succ.solar_punk.block.entity.custom.FermentationVatBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// The vat's item slot takes anything a fermenting recipe uses
@Mixin(targets = "net.succ.solar_punk.block.entity.custom.FermentationVatBlockEntity$3", remap = false)
public abstract class FermentationVatInputsMixin {
    @Shadow
    @Final
    FermentationVatBlockEntity this$0;

    @Inject(method = "isItemValid", at = @At("HEAD"), cancellable = true)
    private void materialismcore$recipeItems(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(Fermenting.isItemInput(this$0.getLevel(), stack));
    }
}
