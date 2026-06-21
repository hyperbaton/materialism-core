package com.pjbhva.materialismcore.registry;

import com.pjbhva.materialismcore.MaterialismCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MaterialismCore.MOD_ID);

    public static final Supplier<CreativeModeTab> MATERIALISM_TAB = CREATIVE_TABS.register("materialism_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.materialismcore"))
                    .icon(() -> ModItems.GALVANIC_CELL.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(ModItems.GALVANIC_CELL.get());
                    })
                    .build());
}
