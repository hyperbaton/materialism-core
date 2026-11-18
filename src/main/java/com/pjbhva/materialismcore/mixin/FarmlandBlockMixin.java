package com.pjbhva.materialismcore.mixin;

import com.pjbhva.materialismcore.compat.Irrigation;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Sprinkler-irrigated farmland keeps at least the irrigation floor of hydration, in growth and in tooltips
@Mixin(value = FarmlandBlock.class, remap = false)
public abstract class FarmlandBlockMixin {
    @Inject(method = "getInstantHydration(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;J)I", at = @At("HEAD"))
    private static void materialismcore$queriedTick(Level level, BlockPos pos, long calendarTick, CallbackInfoReturnable<Integer> cir) {
        Irrigation.QUERIED_TICK.set(calendarTick);
    }

    @Inject(method = "getInstantHydration(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;J)I", at = @At("RETURN"))
    private static void materialismcore$clearTick(Level level, BlockPos pos, long calendarTick, CallbackInfoReturnable<Integer> cir) {
        Irrigation.QUERIED_TICK.remove();
    }

    @Inject(method = "getInstantHydrationFromRainHydration", at = @At("RETURN"), cancellable = true)
    private static void materialismcore$irrigation(Level level, BlockPos pos, int rainHydration, CallbackInfoReturnable<Integer> cir) {
        int floor = Irrigation.floor(level, pos);
        if (floor > cir.getReturnValueI()) {
            cir.setReturnValue(floor);
        }
    }
}
