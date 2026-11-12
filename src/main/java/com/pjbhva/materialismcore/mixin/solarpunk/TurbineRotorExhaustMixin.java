package com.pjbhva.materialismcore.mixin.solarpunk;

import com.pjbhva.materialismcore.steam.SteamCycle;
import net.minecraft.world.level.material.FlowingFluid;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// The turbine releases exhaust steam instead of condensate water
@Pseudo
@Mixin(targets = "net.succ.solar_punk.block.entity.custom.TurbineRotorBlockEntity", remap = false)
public abstract class TurbineRotorExhaustMixin {
    @Redirect(method = "tick", at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/level/material/Fluids;WATER:Lnet/minecraft/world/level/material/FlowingFluid;",
            opcode = Opcodes.GETSTATIC))
    private FlowingFluid materialismcore$exhaust() {
        return SteamCycle.exhaustOrWater();
    }
}
