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

        boolean recheckRecipe = false;
        for (ItemEntity entity : entities) {
            ItemStack stack = entity.getItem();

            for (int i = 0; i < 64 && !stack.isEmpty(); i++) {
                if (stack.is(TFMGTags.TFMGItemTags.BLAST_FURNACE_FUEL.tag)) {
                    if (self.fuel >= 64) break;
                    self.fuel++;
                    stack.shrink(1);
                    recheckRecipe = true;
                    continue;
                }
                if (stack.is(TFMGTags.TFMGItemTags.FLUX.tag)) {
                    if (!materialismcore$offer(self.fluxInventory, stack)) break;
                    recheckRecipe = true;
                    continue;
                }
                if (!materialismcore$offer(self.inputInventory, stack)) break;
                recheckRecipe = true;
            }
        }

        if (recheckRecipe && self.timer <= -1) {
            self.executeRecipe();
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
