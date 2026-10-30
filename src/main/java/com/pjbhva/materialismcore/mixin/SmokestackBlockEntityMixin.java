package com.pjbhva.materialismcore.mixin;

import com.drmangotea.tfmg.content.machinery.misc.smokestack.SmokestackBlockEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A smokestack column moves its gas up one segment per tick by draining its own tank and filling
 * the segment above with a brand new CO2 stack, whatever it actually drained. Once
 * {@link ExhaustTankMixin} lets other gases in, that would turn them into CO2 on the way up (and
 * void them if the segment above already holds a different gas). Fill the segment above with the
 * stack that was really drained instead.
 */
@Mixin(SmokestackBlockEntity.class)
public abstract class SmokestackBlockEntityMixin {
    // The second drain in tick() is the transfer to the segment above; the first vents at the top.
    @WrapOperation(
            method = "tick()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/fluids/capability/templates/FluidTank;drain(ILnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/neoforged/neoforge/fluids/FluidStack;",
                    ordinal = 1
            )
    )
    private FluidStack materialismcore$rememberTransferred(FluidTank tank, int amount, IFluidHandler.FluidAction action,
                                                           Operation<FluidStack> original,
                                                           @Share("transferred") LocalRef<FluidStack> transferred) {
        FluidStack drained = original.call(tank, amount, action);
        transferred.set(drained);
        return drained;
    }

    @WrapOperation(
            method = "tick()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/fluids/capability/templates/FluidTank;fill(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)I"
            )
    )
    private int materialismcore$fillTransferred(FluidTank tank, FluidStack stack, IFluidHandler.FluidAction action,
                                                Operation<Integer> original,
                                                @Share("transferred") LocalRef<FluidStack> transferred) {
        FluidStack drained = transferred.get();
        return original.call(tank, drained == null || drained.isEmpty() ? stack : drained, action);
    }
}
