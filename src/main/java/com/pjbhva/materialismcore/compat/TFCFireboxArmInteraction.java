package com.pjbhva.materialismcore.compat;

import com.pjbhva.materialismcore.MaterialismCore;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes.DepositOnlyArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Lets the Create mechanical arm feed fuel into a TFC firebox. Create only lets the arm target
 * blocks with a registered interaction point type, and its default point asks for the item handler
 * on the top face, while TFC only exposes the firebox's fuel slots on its horizontal faces.
 */
public final class TFCFireboxArmInteraction {

    private static final DeferredRegister<ArmInteractionPointType> ARM_INTERACTION_POINT_TYPES =
            DeferredRegister.create(CreateRegistries.ARM_INTERACTION_POINT_TYPE, MaterialismCore.MOD_ID);

    static {
        ARM_INTERACTION_POINT_TYPES.register("tfc_firebox", FireboxType::new);
    }

    private TFCFireboxArmInteraction() {}

    public static void register(IEventBus modEventBus) {
        ARM_INTERACTION_POINT_TYPES.register(modEventBus);
    }

    private static class FireboxType extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.is(TFCBlocks.FIREBOX.get());
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new FireboxPoint(this, level, pos, state);
        }
    }

    // Deposit-only: the only thing TFC lets out of a firebox (slot 0, from below) is the fuel
    // currently burning, which nothing should be pulling out with an arm.
    private static class FireboxPoint extends DepositOnlyArmInteractionPoint {
        FireboxPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        @Override
        protected IItemHandler getHandler(ArmBlockEntity armBlockEntity) {
            // Every horizontal face gets the same fuel-insertion handler, so any one of them will do.
            return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.NORTH);
        }
    }
}
