package com.pjbhva.materialismcore.mixin;

import com.drmangotea.tfmg.base.TFMGUtils;
import com.drmangotea.tfmg.content.machinery.misc.exhaust.ExhaustBlockEntity;
import com.pjbhva.materialismcore.steam.SteamCycle;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Exhausts venting steam puff white clouds instead of smoke
@Mixin(value = ExhaustBlockEntity.class, remap = false)
public abstract class ExhaustBlockEntityMixin {
    @Unique
    private boolean materialismcore$steam;

    @Inject(method = "tick", at = @At("HEAD"))
    private void materialismcore$trackSteam(CallbackInfo ci) {
        ExhaustBlockEntity self = (ExhaustBlockEntity) (Object) this;
        if (!self.tankInventory.isEmpty()) {
            materialismcore$steam = SteamCycle.isSteam(self.tankInventory.getFluid().getFluid());
        }
    }

    @Redirect(method = "makeParticles", at = @At(value = "INVOKE",
            target = "Lcom/drmangotea/tfmg/base/TFMGUtils;spawnSmokeParticles(Lnet/minecraft/world/level/Level;DDD)V"))
    private void materialismcore$particles(Level level, double x, double y, double z) {
        if (materialismcore$steam) {
            SteamCycle.spawnSteamParticles(level, x, y, z, 0, 0.08, 0);
        } else {
            TFMGUtils.spawnSmokeParticles(level, x, y, z);
        }
    }
}
