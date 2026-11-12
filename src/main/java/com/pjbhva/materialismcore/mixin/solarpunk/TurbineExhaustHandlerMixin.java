package com.pjbhva.materialismcore.mixin.solarpunk;

import com.pjbhva.materialismcore.steam.SteamCycle;
import net.minecraft.world.level.material.FlowingFluid;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Pipes drain exhaust steam from the turbine casing
@Pseudo
@Mixin(targets = "net.succ.solar_punk.block.entity.custom.TurbineRotorBlockEntity$3", remap = false)
public abstract class TurbineExhaustHandlerMixin {
    @Redirect(method = "drain(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/neoforged/neoforge/fluids/FluidStack;",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/world/level/material/Fluids;WATER:Lnet/minecraft/world/level/material/FlowingFluid;",
                    opcode = Opcodes.GETSTATIC))
    private FlowingFluid materialismcore$exhaust() {
        return SteamCycle.exhaustOrWater();
    }
}
