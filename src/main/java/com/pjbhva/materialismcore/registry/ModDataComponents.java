package com.pjbhva.materialismcore.registry;

import com.mojang.serialization.Codec;
import com.pjbhva.materialismcore.MaterialismCore;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MaterialismCore.MOD_ID);

    public static final Supplier<DataComponentType<Integer>> STORED_ENERGY =
            DATA_COMPONENTS.register("stored_energy",
                    () -> DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT)
                            .build());

    public static final Supplier<DataComponentType<FluidStack>> SCANNED_FLUID =
            DATA_COMPONENTS.register("scanned_fluid",
                    () -> DataComponentType.<FluidStack>builder()
                            .persistent(FluidStack.OPTIONAL_CODEC)
                            .networkSynchronized(FluidStack.OPTIONAL_STREAM_CODEC)
                            .build());
}
