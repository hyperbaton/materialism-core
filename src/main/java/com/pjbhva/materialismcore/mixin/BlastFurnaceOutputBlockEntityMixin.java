package com.pjbhva.materialismcore.mixin;

import com.drmangotea.tfmg.content.machinery.metallurgy.blast_furnace.BlastFurnaceOutputBlockEntity;
import com.drmangotea.tfmg.registry.TFMGTags;
import com.simibubi.create.foundation.item.SmartInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Items thrown into the blast furnace opening are sorted by {@code collectItems()}. TFMG's version
 * lets items fall through into the ore slot: fuel arriving at 64 fuel ends up there, and flux
 * spills into it too (even right after being placed in the flux slot), blocking the recipe. Stop
 * at the first item that can't be placed in its own slot instead, leaving the rest in the
 * opening. (The top hatch's {@code fillFurnace()} already routes correctly.)
 */
@Mixin(BlastFurnaceOutputBlockEntity.class)
public abstract class BlastFurnaceOutputBlockEntityMixin {
    @Inject(method = "collectItems()V", at = @At("HEAD"), cancellable = true)
    private void materialismcore$fixSlotRouting(CallbackInfo ci) {
        ci.cancel();

        BlastFurnaceOutputBlockEntity self = (BlastFurnaceOutputBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null) {
            return;
        }

        Direction facing = self.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        BlockPos above = self.getBlockPos().relative(facing.getOpposite()).above();
        List<ItemEntity> entities = level.getEntitiesOfClass(ItemEntity.class, new AABB(above));
        if (entities.isEmpty()) {
            return;
        }

        // No need to recheck the recipe here: lazyTick() looks one up right after collecting.
        for (ItemEntity entity : entities) {
            ItemStack stack = entity.getItem();

            for (int i = 0; i < 64 && !stack.isEmpty(); i++) {
                if (stack.is(TFMGTags.Items.BLAST_FURNACE_FUEL.tag)) {
                    if (self.fuel >= 64) break;
                    self.fuel++;
                    stack.shrink(1);
                    continue;
                }
                if (stack.is(TFMGTags.Items.FLUX.tag)) {
                    if (!materialismcore$offer(self.fluxInventory, stack)) break;
                    continue;
                }
                if (!materialismcore$offer(self.inputInventory, stack)) break;
            }
        }
    }

    private static boolean materialismcore$offer(SmartInventory inventory, ItemStack stack) {
        ItemStack current = inventory.getItem(0);
        if (!inventory.isEmpty() && !current.is(stack.getItem())) {
            return false;
        }
        if (current.getCount() >= stack.getMaxStackSize()) {
            return false;
        }
        inventory.setItem(0, new ItemStack(stack.getItem(), current.getCount() + 1));
        stack.shrink(1);
        return true;
    }
}
