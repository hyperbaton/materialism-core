package com.pjbhva.materialismcore.registry;

import com.pjbhva.materialismcore.MaterialismCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class ModTags {
    public static class Fluids {
        /** Gases TFMG's exhaust and smokestack accept and vent to the atmosphere, on top of its own CO2. */
        public static final TagKey<Fluid> EXHAUSTABLE = create("exhaustable");

        private static TagKey<Fluid> create(String path) {
            return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(MaterialismCore.MOD_ID, path));
        }
    }
}
