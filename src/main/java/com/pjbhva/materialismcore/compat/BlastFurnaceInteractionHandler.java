package com.pjbhva.materialismcore.compat;

import com.drmangotea.tfmg.content.machinery.metallurgy.blast_furnace.BlastFurnaceOutputBlockEntity;
import com.simibubi.create.foundation.item.SmartInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Right-clicking a TFMG blast furnace output block with an empty hand dumps its ore and flux
 * slots back to the player. Without this there's no way to recover a misfed item short of
 * breaking the block
 */
public class BlastFurnaceInteractionHandler {
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getItemStack().isEmpty()) {
            return;
        }
        Level level = event.getLevel();
        if (level.isClientSide) {
            return;
        }
        BlockEntity be = level.getBlockEntity(event.getPos());
        if (!(be instanceof BlastFurnaceOutputBlockEntity furnace)) {
            return;
        }

        Player player = event.getEntity();
        boolean gaveOre = give(furnace.inputInventory, player);
        boolean gaveFlux = give(furnace.fluxInventory, player);

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
        if (!gaveOre && !gaveFlux) {
            player.displayClientMessage(Component.translatable("block.materialismcore.blast_furnace_output.nothing_to_retrieve"), true);
        }
    }

    private static boolean give(SmartInventory inventory, Player player) {
        ItemStack stack = inventory.getItem(0);
        if (stack.isEmpty()) {
            return false;
        }
        inventory.setItem(0, ItemStack.EMPTY);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        return true;
    }
}
