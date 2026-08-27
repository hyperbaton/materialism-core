package com.pjbhva.materialismcore.mixin;

import com.drmangotea.tfmg.content.machinery.metallurgy.blast_furnace.BlastFurnaceHatchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BlastFurnaceHatchBlockEntity.class)
public abstract class BlastFurnaceHatchBlockEntityMixin {
    private static final int MAX_SHAFT_SCAN = 32;

    @Inject(method = "dropItems()V", at = @At("HEAD"), cancellable = true)
    private void materialismcore$holdIfShaftOccupied(CallbackInfo ci) {
        BlastFurnaceHatchBlockEntity self = (BlastFurnaceHatchBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null) {
            return;
        }

        BlockPos pos = self.getBlockPos();
        BlockPos bottom = pos;
        for (int i = 0; i < MAX_SHAFT_SCAN; i++) {
            BlockPos next = bottom.below();
            BlockState state = level.getBlockState(next);
            if (!state.isAir()) {
                break;
            }
            bottom = next;
        }
        if (bottom.equals(pos)) {
            // Nothing open below at all -- the original method's own air check will bail out too.
            return;
        }

        AABB shaft = new AABB(pos.getX(), bottom.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        List<ItemEntity> alreadyFalling = level.getEntitiesOfClass(ItemEntity.class, shaft);
        if (!alreadyFalling.isEmpty()) {
            ci.cancel();
        }
    }
}
