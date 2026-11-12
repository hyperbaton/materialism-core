package com.pjbhva.materialismcore.steam;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

// Fluids of the steam cycle: pressurized steam from boilers, exhaust steam from turbines
public final class SteamCycle {
    private static final ResourceLocation PRESSURIZED = ResourceLocation.fromNamespaceAndPath("solarpunk", "steam");
    private static final ResourceLocation EXHAUST = ResourceLocation.fromNamespaceAndPath("chemica", "steam");

    private SteamCycle() {}

    public static Fluid pressurizedSteam() {
        return BuiltInRegistries.FLUID.get(PRESSURIZED);
    }

    public static Fluid exhaustSteam() {
        return BuiltInRegistries.FLUID.get(EXHAUST);
    }

    // Falls back to water when Chemica is missing, which is Solarpunk's own behaviour
    public static FlowingFluid exhaustOrWater() {
        return exhaustSteam() instanceof FlowingFluid flowing && flowing != Fluids.EMPTY ? flowing : Fluids.WATER;
    }

    public static boolean isSteam(Fluid fluid) {
        return fluid != Fluids.EMPTY && (fluid.isSame(pressurizedSteam()) || fluid.isSame(exhaustSteam()));
    }

    // White steam puffs, used where TFMG would draw smoke
    public static void spawnSteamParticles(Level level, double x, double y, double z, double dx, double dy, double dz) {
        RandomSource random = level.getRandom();
        for (int i = random.nextInt(3); i > 0; i--) {
            level.addParticle(ParticleTypes.CLOUD,
                    x + (random.nextDouble() - 0.5) * 0.3, y, z + (random.nextDouble() - 0.5) * 0.3,
                    dx + (random.nextDouble() - 0.5) * 0.02, dy, dz + (random.nextDouble() - 0.5) * 0.02);
        }
    }
}
