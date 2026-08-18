package com.pjbhva.materialismcore;

import com.pjbhva.materialismcore.block.GalvanicCellEnergyStorage;
import com.pjbhva.materialismcore.compat.TFCFireboxBoilerHeater;
import com.pjbhva.materialismcore.registry.ModBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = MaterialismCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEvents {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.GALVANIC_CELL.get(),
                (blockEntity, direction) -> new GalvanicCellEnergyStorage(blockEntity)
        );
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded("create")) {
            event.enqueueWork(TFCFireboxBoilerHeater::register);
        }
    }
}
