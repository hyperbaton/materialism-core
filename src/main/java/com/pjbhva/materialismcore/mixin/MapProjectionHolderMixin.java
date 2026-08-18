package com.pjbhva.materialismcore.mixin;

import net.trygve55.eratosthenes.MapProjectionHolder;
import net.trygve55.eratosthenes.compat.TFCRealWorld;
import net.trygve55.eratosthenes.config.MapProjectionConfig;
import net.trygve55.eratosthenes.config.ServerConfig;
import net.trygve55.eratosthenes.mapprojections.MapProjection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code MapProjectionHolder.set()} is only ever called from tfc_eratosthenes' own mixins into
 * TFC's {@code RegionGenerator}/{@code OverworldClimateModel} constructors. On some worlds that
 * injection never fires before the first player tick (observed after TFC updated past what
 * tfc_eratosthenes 0.2.2 was built against), leaving {@code currentMapProjection} null forever --
 * every {@code get()} call then throws {@code IllegalStateException}, crashing the world on the
 * very first player movement tick.
 * <p>
 * This makes {@code get()} self-heal: if nothing has set a projection yet, compute one from the
 * mod's own config (same projection type players configured, real-world half-meridian if that
 * compat is loaded) instead of crashing. If the upstream mixin does eventually fire, it overwrites
 * this with the real value via {@code set()} as normal.
 */
@Mixin(MapProjectionHolder.class)
public abstract class MapProjectionHolderMixin {

    @Shadow
    private static MapProjection currentMapProjection;

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private static void materialismcore$fallbackToDefault(CallbackInfoReturnable<MapProjection> cir) {
        if (currentMapProjection != null) {
            return;
        }
        MapProjectionConfig config = ServerConfig.getOrDefault(ServerConfig.MAP_PROJECTION).get();
        int halfMeridian = TFCRealWorld.isLoaded() ? TFCRealWorld.getHalfMeridian() : 0;
        MapProjection projection = config.toMapProjection(halfMeridian);
        currentMapProjection = projection;
        cir.setReturnValue(projection);
    }
}
