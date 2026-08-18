package com.pjbhva.materialismcore.mixin;

import net.createmod.catnip.config.ConfigBase;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Every Create-family mod (Create itself, TFMG, VI, ...) reads its config through Ponder's
 * {@code ConfigBase$CValue}, which just calls straight through to the underlying
 * {@code ModConfigSpec$ConfigValue.get()}. If that's called before NeoForge has actually loaded
 * the backing config file for that spec, it throws IllegalStateException and -- because this
 * happens most often from inside creative-tab-content-building event listeners -- takes the
 * whole server down with it (observed: vintageimprovements' creative tab lambda, triggered by
 * CC:Tweaked forcing a tab rebuild on server start, reading a config value whose spec hadn't
 * loaded yet on that particular boot).
 * <p>
 * Rather than patch every mod that might hit this ordering issue individually, this makes the
 * read itself safe at the shared source: on that specific failure, fall back to the config
 * value's own declared default instead of crashing.
 */
@Mixin(ConfigBase.CValue.class)
public abstract class ConfigBaseCValueMixin {

    @Redirect(
            method = "get",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/common/ModConfigSpec$ConfigValue;get()Ljava/lang/Object;"
            )
    )
    private Object materialismcore$safeGet(ModConfigSpec.ConfigValue<?> configValue) {
        try {
            return configValue.get();
        } catch (IllegalStateException e) {
            return configValue.getDefault();
        }
    }
}
