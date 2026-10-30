package com.pjbhva.materialismcore.compat;

import com.mojang.serialization.MapCodec;
import com.pjbhva.materialismcore.MaterialismCore;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.chest.ChestMountedStorage;
import com.simibubi.create.api.contraption.storage.item.menu.StorageInteractionWrapper;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorage;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorageType;
import com.simibubi.create.api.registry.CreateRegistries;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.dries007.tfc.common.blockentities.TFCChestBlockEntity;
import net.dries007.tfc.common.blocks.wood.TFCChestBlock;
import net.dries007.tfc.common.container.RestrictedChestContainer;
import net.dries007.tfc.common.container.TFCContainerTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Makes TFC chests (and every addon chest built on {@link TFCChestBlock}, such as AFC's) work as
 * contraption storage. Create only mounts vanilla chests through the {@code create:chest_mounted_storage}
 * tag, and its fallback storage only accepts item handlers that are exactly {@code ItemStackHandler},
 * while TFC exposes its chests through its own {@code InventoryWrapper}. So without this the chest
 * is moved as a plain block: no contraption inventory, no menu while moving.
 *
 * <p>Unlike adding the blocks to Create's chest tag, this keeps TFC's chest rules while mounted:
 * items above the configured chest size are refused, and the menu is TFC's restricted chest menu.
 */
public final class TFCChestMountedStorage {

    private static final DeferredRegister<MountedItemStorageType<?>> MOUNTED_ITEM_STORAGE_TYPES =
            DeferredRegister.create(CreateRegistries.MOUNTED_ITEM_STORAGE_TYPE, MaterialismCore.MOD_ID);

    private static final DeferredHolder<MountedItemStorageType<?>, Type> TFC_CHEST =
            MOUNTED_ITEM_STORAGE_TYPES.register("tfc_chest", Type::new);

    private TFCChestMountedStorage() {}

    public static void register(IEventBus modEventBus) {
        MOUNTED_ITEM_STORAGE_TYPES.register(modEventBus);
        // Providers registered later take priority, so this wins over Create's fallback provider.
        modEventBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(() ->
                MountedItemStorageType.REGISTRY.registerProvider(
                        block -> block instanceof TFCChestBlock ? TFC_CHEST.get() : null)));
    }

    private static class Type extends SimpleMountedStorageType<Storage> {
        Type() {
            super(Storage.CODEC);
        }

        @Override
        protected IItemHandler getHandler(Level level, BlockEntity be) {
            // Same as Create's chest type: wrap this half only, so each half of a double chest is
            // its own storage and the menu joins them back together.
            return be instanceof Container container ? new InvWrapper(container) : null;
        }

        @Override
        protected SimpleMountedStorage createStorage(IItemHandler handler) {
            return new Storage(handler);
        }
    }

    private static class Storage extends ChestMountedStorage {
        static final MapCodec<Storage> CODEC = SimpleMountedStorage.codec(Storage::new);

        Storage(IItemHandler handler) {
            super(TFC_CHEST.get(), handler);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return TFCChestBlockEntity.isValid(stack) && super.isItemValid(slot, stack);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            // The wrapped ItemStackHandler only checks its own isItemValid, not the override above.
            return stack.isEmpty() || TFCChestBlockEntity.isValid(stack) ? super.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        protected @Nullable MenuProvider createMenuProvider(Component name, IItemHandlerModifiable handler,
                                                            Predicate<Player> stillValid, Consumer<Player> onClose) {
            int rows = handler.getSlots() / 9;
            MenuType<?> menuType = switch (handler.getSlots()) {
                case 18 -> TFCContainerTypes.CHEST_9x2.get();
                case 36 -> TFCContainerTypes.CHEST_9x4.get();
                default -> null;
            };
            if (menuType == null) {
                return super.createMenuProvider(name, handler, stillValid, onClose);
            }
            Container container = new StorageInteractionWrapper(handler, stillValid, onClose);
            return new SimpleMenuProvider(
                    (id, inventory, player) -> new RestrictedChestContainer(menuType, id, inventory, container, rows),
                    name);
        }
    }
}
