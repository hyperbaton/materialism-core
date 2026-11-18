package com.pjbhva.materialismcore.mixin.solarpunk;

import com.pjbhva.materialismcore.compat.Sprinklers;
import net.minecraft.network.chat.Component;
import net.succ.solar_punk.block.entity.custom.KineticSprinklerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

// The sprinkler irrigates, fertilizes or weeds TFC soil instead of bone-mealing
@Mixin(value = KineticSprinklerBlockEntity.class, remap = false)
public abstract class KineticSprinklerBlockEntityMixin {
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/blockEntity/SmartBlockEntity;tick()V", shift = At.Shift.AFTER), cancellable = true)
    private void materialismcore$spray(CallbackInfo ci) {
        ci.cancel();
        Sprinklers.tick((KineticSprinklerBlockEntity) (Object) this);
    }

    @Inject(method = "addToGoggleTooltip", at = @At("HEAD"), cancellable = true)
    private void materialismcore$tooltip(List<Component> tooltip, boolean sneaking, CallbackInfoReturnable<Boolean> cir) {
        Sprinklers.tooltip((KineticSprinklerBlockEntity) (Object) this, tooltip);
        cir.setReturnValue(true);
    }
}
