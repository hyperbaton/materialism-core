package com.pjbhva.materialismcore.mixin.electroenergetics;

import com.george_vi.electroenergetics.content.converter.ConverterBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Shows the converter's voltage setting in output mode too, where it sets the TFMG supply voltage.
 */
@Mixin(targets = "com.george_vi.electroenergetics.content.converter.ConverterBlockEntity$ValueBox", remap = false)
public abstract class ConverterValueBoxMixin {
    @Inject(method = "isSideActive", at = @At("HEAD"), cancellable = true)
    private void materialism$alwaysActive(BlockState state, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(direction.getAxis() == state.getValue(ConverterBlock.FACING).getAxis());
    }
}
