package com.pjbhva.materialismcore.compat;

import com.pjbhva.materialismcore.ModConfigs;
import com.pjbhva.materialismcore.registry.ModAttachments;
import com.pjbhva.materialismcore.registry.ModDataComponents;
import dan200.computercraft.shared.computer.blocks.ComputerBlockEntity;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.pocket.items.PocketComputerItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.ComponentEnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

/** ComputerCraft computers run on FE: desktops from a block buffer, pocket computers from a battery. */
public final class ComputerPower {
    private ComputerPower() {}

    /** Command computers are admin tools and stay free. */
    public static boolean isPowered(ComputerFamily family) {
        return family != ComputerFamily.COMMAND;
    }

    public static int desktopDraw(ComputerFamily family) {
        return family == ComputerFamily.ADVANCED
                ? ModConfigs.ADVANCED_COMPUTER_ENERGY_PER_TICK.get()
                : ModConfigs.COMPUTER_ENERGY_PER_TICK.get();
    }

    public static int pocketDraw(ComputerFamily family) {
        return family == ComputerFamily.ADVANCED
                ? ModConfigs.ADVANCED_POCKET_COMPUTER_ENERGY_PER_TICK.get()
                : ModConfigs.POCKET_COMPUTER_ENERGY_PER_TICK.get();
    }

    public static int pocketCapacity(ComputerFamily family) {
        return family == ComputerFamily.ADVANCED
                ? ModConfigs.ADVANCED_POCKET_COMPUTER_CAPACITY.get()
                : ModConfigs.POCKET_COMPUTER_CAPACITY.get();
    }

    public static int stored(ComputerBlockEntity computer) {
        return computer.getData(ModAttachments.COMPUTER_ENERGY.get());
    }

    public static boolean canRun(ComputerBlockEntity computer) {
        return !isPowered(computer.getFamily()) || stored(computer) >= desktopDraw(computer.getFamily());
    }

    public static int charge(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.STORED_ENERGY.get(), 0);
    }

    public static boolean canRun(ItemStack stack) {
        return !(stack.getItem() instanceof PocketComputerItem pocket)
                || !isPowered(pocket.getFamily())
                || charge(stack) >= pocketDraw(pocket.getFamily());
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (String id : new String[]{"computer_normal", "computer_advanced"}) {
            BlockEntityType<?> type = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("computercraft", id));
            if (type != null) {
                registerBlock(event, type);
            }
        }
        for (String id : new String[]{"pocket_computer_normal", "pocket_computer_advanced"}) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("computercraft", id));
            if (item instanceof PocketComputerItem pocket) {
                event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, ctx) -> new ComponentEnergyStorage(
                        stack, ModDataComponents.STORED_ENERGY.get(), pocketCapacity(pocket.getFamily()), Integer.MAX_VALUE, 0), item);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends net.minecraft.world.level.block.entity.BlockEntity> void registerBlock(RegisterCapabilitiesEvent event, BlockEntityType<?> type) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, (BlockEntityType<T>) type, (be, side) ->
                be instanceof ComputerBlockEntity computer && isPowered(computer.getFamily()) ? new Buffer(computer) : null);
    }

    /** Receive-only FE buffer stored on the computer block entity. */
    private record Buffer(ComputerBlockEntity computer) implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int stored = stored(computer);
            int received = Math.max(0, Math.min(maxReceive, ModConfigs.COMPUTER_BUFFER.get() - stored));
            if (!simulate && received > 0) {
                computer.setData(ModAttachments.COMPUTER_ENERGY.get(), stored + received);
                computer.setChanged();
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return stored(computer);
        }

        @Override
        public int getMaxEnergyStored() {
            return ModConfigs.COMPUTER_BUFFER.get();
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}
