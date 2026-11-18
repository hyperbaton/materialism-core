package com.pjbhva.materialismcore.mixin.solarpunk;

import com.pjbhva.materialismcore.ModConfigs;
import com.pjbhva.materialismcore.compat.Sprinklers;
import net.dries007.tfc.common.blockentities.FarmlandBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

// The hoe overlay tells when a running sprinkler irrigates the farmland
@Mixin(value = FarmlandBlockEntity.class, remap = false)
public abstract class FarmlandIrrigationOverlayMixin {
    @Inject(method = "addHoeOverlayInfo", at = @At("TAIL"))
    private void materialismcore$irrigated(Level level, BlockPos pos, Consumer<Component> text, boolean isCrop, boolean debug, CallbackInfo ci) {
        if (Sprinklers.irrigatedBySprinkler(level, pos)) {
            text.accept(Component.translatable("materialismcore.sprinkler.overlay", ModConfigs.SPRINKLER_HYDRATION_FLOOR.get())
                    .withStyle(ChatFormatting.AQUA));
        }
    }
}
