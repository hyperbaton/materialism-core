package com.pjbhva.materialismcore.block;

import com.mojang.serialization.MapCodec;
import com.pjbhva.materialismcore.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class GalvanicCellBlock extends BaseEntityBlock {
    public static final int MAX_ENERGY = 12000;
    public static final int MAX_EXTRACT = 256;

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1, 0, 5, 7, 10, 11),
            Block.box(9, 0, 5, 15, 10, 11),
            Block.box(6, 0, 1, 10, 6, 9),
            Block.box(5, 5, 0, 11, 11, 3),
            Block.box(2, 7, 1, 14, 8, 5)
    );

    public static final MapCodec<GalvanicCellBlock> CODEC = simpleCodec(GalvanicCellBlock::new);

    public GalvanicCellBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GalvanicCellBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.GALVANIC_CELL.get(), GalvanicCellBlockEntity::serverTick);
    }

}
