package com.pjbhva.materialismcore.item;

import com.pjbhva.materialismcore.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;

/**
 * Reads the identity of a fluid from whatever it's pointed at
 */
public class FluidScannerItem extends Item {
    public FluidScannerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        boolean secondTank = player != null && player.isShiftKeyDown();

        FluidStack scanned = scan(level, pos, context.getClickedFace(), secondTank);
        if (scanned.isEmpty()) {
            if (!level.isClientSide && player != null) {
                player.displayClientMessage(Component.translatable("item.materialismcore.fluid_scanner.none_found"), true);
            }
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            context.getItemInHand().set(ModDataComponents.SCANNED_FLUID.get(), scanned);
            if (player != null) {
                player.displayClientMessage(Component.translatable("item.materialismcore.fluid_scanner.scanned",
                        scanned.getHoverName()), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static FluidStack scan(Level level, BlockPos pos, Direction face, boolean secondTank) {
        IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, face);
        if (handler != null) {
            FluidStack first = FluidStack.EMPTY;
            FluidStack second = FluidStack.EMPTY;
            for (int i = 0; i < handler.getTanks(); i++) {
                FluidStack inTank = handler.getFluidInTank(i);
                if (inTank.isEmpty()) continue;
                if (first.isEmpty()) {
                    first = inTank;
                } else if (second.isEmpty()) {
                    second = inTank;
                    break;
                }
            }
            FluidStack chosen = secondTank && !second.isEmpty() ? second : first;
            if (!chosen.isEmpty()) {
                return new FluidStack(chosen.getFluid(), 1000);
            }
        }

        FluidState fluidState = level.getFluidState(pos);
        if (!fluidState.isEmpty()) {
            return new FluidStack(fluidState.getType(), 1000);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        FluidStack scanned = stack.get(ModDataComponents.SCANNED_FLUID.get());
        if (scanned != null && !scanned.isEmpty()) {
            tooltip.add(Component.translatable("item.materialismcore.fluid_scanner.tooltip", scanned.getHoverName()));
        } else {
            tooltip.add(Component.translatable("item.materialismcore.fluid_scanner.tooltip_empty"));
        }
    }
}
