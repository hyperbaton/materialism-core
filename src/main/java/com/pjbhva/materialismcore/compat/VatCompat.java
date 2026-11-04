package com.pjbhva.materialismcore.compat;

import com.pjbhva.materialismcore.ModConfigs;
import net.dries007.tfc.common.capabilities.BlockCapabilities;
import net.dries007.tfc.common.component.heat.IHeatConsumer;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

public final class VatCompat {

    private VatCompat() {}

    /** TFC heat sources push their temperature into the block above them, from its underside. */
    @SuppressWarnings("unchecked")
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                BlockCapabilities.HEAT,
                (BlockEntityType<BlockEntity>) BuiltInRegistries.BLOCK_ENTITY_TYPE.get(tfmg("chemical_vat")),
                (vat, side) -> side == Direction.DOWN ? (IHeatConsumer) vat : null
        );
    }

    /** Electrodes already stack to one, so they can take durability like any tool. */
    public static void addElectrodeDurability(ModifyDefaultComponentsEvent event) {
        addDurability(event, "copper_electrode", ModConfigs.COPPER_ELECTRODE_DURABILITY);
        addDurability(event, "zinc_electrode", ModConfigs.ZINC_ELECTRODE_DURABILITY);
        addDurability(event, "graphite_electrode", ModConfigs.GRAPHITE_ELECTRODE_DURABILITY);
    }

    private static void addDurability(ModifyDefaultComponentsEvent event, String item, ModConfigSpec.IntValue durability) {
        event.modify(BuiltInRegistries.ITEM.get(tfmg(item)), components -> components
                .set(DataComponents.MAX_DAMAGE, durability.get())
                .set(DataComponents.DAMAGE, 0));
    }

    private static ResourceLocation tfmg(String path) {
        return ResourceLocation.fromNamespaceAndPath("tfmg", path);
    }
}
