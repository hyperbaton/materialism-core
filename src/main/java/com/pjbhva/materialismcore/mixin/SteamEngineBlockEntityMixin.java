package com.pjbhva.materialismcore.mixin;

import com.pjbhva.materialismcore.steam.SteamOutlet;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Engines with a pipe instead of a shaft produce pressurized steam
@Mixin(SteamEngineBlockEntity.class)
public abstract class SteamEngineBlockEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void materialismcore$produceSteam(CallbackInfo ci) {
        SteamOutlet.tick((SteamEngineBlockEntity) (Object) this);
    }
}
