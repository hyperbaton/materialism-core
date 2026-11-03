package com.pjbhva.materialismcore.mixin;

import com.drmangotea.tfmg.content.machinery.metallurgy.casting_basin.CastingBasinBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TFMG's casting basin holds 90mB, Create's ingot size, but the pack's casting recipes use TFC's
 * 100mB ingots, which a 90mB basin can never hold enough of. Resize the tank to one TFC ingot.
 */
@Mixin(CastingBasinBlockEntity.class)
public abstract class CastingBasinBlockEntityMixin {
    private static final int CAPACITY = 100;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void materialismcore$resizeTank(CallbackInfo ci) {
        ((CastingBasinBlockEntity) (Object) this).tank.setCapacity(CAPACITY);
    }
}
