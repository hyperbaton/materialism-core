package com.pjbhva.materialismcore.block;

import com.pjbhva.materialismcore.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class GalvanicCellBlockItem extends BlockItem {
    public GalvanicCellBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    private int getEnergy(ItemStack stack) {
        Integer stored = stack.get(ModDataComponents.STORED_ENERGY.get());
        return stored != null ? stored : GalvanicCellBlock.MAX_ENERGY;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getEnergy(stack) < GalvanicCellBlock.MAX_ENERGY;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * getEnergy(stack) / GalvanicCellBlock.MAX_ENERGY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float ratio = (float) getEnergy(stack) / GalvanicCellBlock.MAX_ENERGY;
        return Mth.color(ratio * 0.4f, ratio * 0.8f + 0.2f, 0.1f);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        int energy = getEnergy(stack);
        tooltip.add(Component.translatable("tooltip.materialismcore.energy", energy, GalvanicCellBlock.MAX_ENERGY));
    }
}
