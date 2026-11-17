package com.pjbhva.materialismcore.mixin.computercraft;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.pjbhva.materialismcore.compat.ComputerPower;
import com.pjbhva.materialismcore.registry.ModDataComponents;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.pocket.core.PocketBrain;
import dan200.computercraft.shared.pocket.core.PocketHolder;
import dan200.computercraft.shared.pocket.core.PocketServerComputer;
import dan200.computercraft.shared.pocket.items.PocketComputerItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Pocket computers run on a battery held on the item. A running one drains it once a second and shuts
 * down when it is empty; an empty one won't boot. Any FE item charger, such as the New Age energiser, refills it.
 */
@Mixin(value = PocketComputerItem.class, remap = false)
public abstract class PocketComputerItemMixin extends Item {
    private PocketComputerItemMixin(Properties properties) {
        super(properties);
    }

    @Shadow
    public abstract ComputerFamily getFamily();

    @Inject(method = "tick(Lnet/minecraft/world/item/ItemStack;Ldan200/computercraft/shared/pocket/core/PocketHolder;Ldan200/computercraft/shared/pocket/core/PocketBrain;)V", at = @At("HEAD"))
    private void materialismcore$drainBattery(ItemStack stack, PocketHolder holder, PocketBrain brain, CallbackInfo ci) {
        if (!ComputerPower.isPowered(getFamily())) {
            return;
        }
        PocketServerComputer computer = brain.computer();
        if (!computer.isOn() || holder.level().getGameTime() % 20 != 0) {
            return;
        }
        int draw = ComputerPower.pocketDraw(getFamily()) * 20;
        int charge = ComputerPower.charge(stack);
        if (charge < draw) {
            computer.shutdown();
            stack.set(ModDataComponents.STORED_ENERGY.get(), 0);
        } else {
            stack.set(ModDataComponents.STORED_ENERGY.get(), charge - draw);
        }
        holder.setChanged();
    }

    @WrapWithCondition(method = "use", at = @At(value = "INVOKE", target = "Ldan200/computercraft/shared/pocket/core/PocketServerComputer;turnOn()V"))
    private boolean materialismcore$bootIfCharged(PocketServerComputer computer,
                                                  @Local(argsOnly = true) Player player, @Local(argsOnly = true) InteractionHand hand) {
        if (ComputerPower.canRun(player.getItemInHand(hand))) {
            return true;
        }
        player.displayClientMessage(Component.translatable("materialismcore.computer.battery_empty"), true);
        return false;
    }

    @WrapWithCondition(method = "getOrCreateBrain", at = @At(value = "INVOKE", target = "Ldan200/computercraft/shared/pocket/core/PocketServerComputer;turnOn()V"))
    private boolean materialismcore$resumeIfCharged(PocketServerComputer computer, @Local(argsOnly = true) ItemStack stack) {
        return ComputerPower.canRun(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return ComputerPower.isPowered(getFamily());
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13f * materialismcore$fill(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb(materialismcore$fill(stack) / 3f, 1f, 1f);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (ComputerPower.isPowered(getFamily())) {
            tooltip.add(Component.translatable("materialismcore.computer.battery",
                    ComputerPower.charge(stack), ComputerPower.pocketCapacity(getFamily())).withStyle(ChatFormatting.GRAY));
        }
    }

    // The battery changes the item once a second; don't replay the equip animation for it
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !oldStack.is(newStack.getItem());
    }

    private float materialismcore$fill(ItemStack stack) {
        return Mth.clamp(ComputerPower.charge(stack) / (float) ComputerPower.pocketCapacity(getFamily()), 0f, 1f);
    }
}
