package com.pjbhva.materialismcore.mixin;

import com.drmangotea.tfmg.base.TFMGUtils;
import com.drmangotea.tfmg.content.machinery.misc.smokestack.SmokestackBlockEntity;
import com.pjbhva.materialismcore.steam.SteamCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Smokestacks venting steam puff white clouds instead of smoke
@Mixin(value = SmokestackBlockEntity.class, remap = false)
public abstract class SmokestackBlockEntityMixin {
    @Unique
    private boolean materialismcore$steam;

    @Inject(method = "tick", at = @At("HEAD"))
    private void materialismcore$trackSteam(CallbackInfo ci) {
        SmokestackBlockEntity self = (SmokestackBlockEntity) (Object) this;
        if (!self.tankInventory.isEmpty()) {
            materialismcore$steam = SteamCycle.isSteam(self.tankInventory.getFluid().getFluid());
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lcom/drmangotea/tfmg/base/TFMGUtils;spawnSmokeParticles(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void materialismcore$particles(Level level, BlockPos pos) {
        if (materialismcore$steam) {
            SteamCycle.spawnSteamParticles(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0, 0.12, 0);
        } else {
            TFMGUtils.spawnSmokeParticles(level, pos);
        }
    }
}
