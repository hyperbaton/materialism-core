package com.pjbhva.materialismcore.compat;

import com.eerussianguy.firmalife.common.blockentities.ClimateReceiver;
import com.pjbhva.materialismcore.MaterialismCore;
import com.pjbhva.materialismcore.ModConfigs;
import com.pjbhva.materialismcore.registry.ModAttachments;
import com.simibubi.create.foundation.utility.CreateLang;
import net.dries007.tfc.common.blockentities.FarmlandBlockEntity;
import net.dries007.tfc.common.blockentities.FarmlandBlockEntity.NutrientType;
import net.dries007.tfc.common.blockentities.IFarmland;
import net.dries007.tfc.common.blocks.crop.DeadCropBlock;
import net.dries007.tfc.common.blocks.crop.DoubleCropBlock;
import net.dries007.tfc.common.blocks.crop.ICropBlock;
import net.dries007.tfc.common.blocks.plant.TFCBushBlock;
import net.dries007.tfc.common.blocks.soil.ConnectedGrassBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.succ.solar_punk.block.entity.custom.KineticSprinklerBlockEntity;
import org.joml.Vector3f;

import java.util.List;

// Solarpunk kinetic sprinkler over TFC soil: irrigation, fertigation and herbicide
public final class Sprinklers {
    public static final TagKey<Fluid> HERBICIDES = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(MaterialismCore.MOD_ID, "herbicides"));
    private static final ResourceLocation FERTILIZER = ResourceLocation.fromNamespaceAndPath("solarpunk", "fertilizer");
    private static final ResourceLocation RICH_FERTILIZER = ResourceLocation.fromNamespaceAndPath("solarpunk", "rich_fertilizer");

    public enum Mode {
        WATER(true, ParticleTypes.SPLASH),
        FERTILIZER(true, new DustParticleOptions(new Vector3f(0.45f, 0.33f, 0.2f), 1f)),
        RICH_FERTILIZER(true, new DustParticleOptions(new Vector3f(0.35f, 0.6f, 0.25f), 1f)),
        HERBICIDE(false, new DustParticleOptions(new Vector3f(0.78f, 0.82f, 0.3f), 1f));

        public final boolean irrigates;
        final ParticleOptions particle;

        Mode(boolean irrigates, ParticleOptions particle) {
            this.irrigates = irrigates;
            this.particle = particle;
        }

        String key() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    private Sprinklers() {}

    public static Mode mode(FluidStack stack) {
        if (stack.isEmpty()) return null;
        if (stack.is(FluidTags.WATER)) return Mode.WATER;
        if (stack.is(HERBICIDES)) return Mode.HERBICIDE;
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        if (FERTILIZER.equals(id)) return Mode.FERTILIZER;
        if (RICH_FERTILIZER.equals(id)) return Mode.RICH_FERTILIZER;
        return null;
    }

    public static int radius(Mode mode) {
        return ModConfigs.SPRINKLER_RADIUS.get() + (mode == Mode.RICH_FERTILIZER ? ModConfigs.SPRINKLER_RICH_BONUS.get() : 0);
    }

    public static boolean active(Mode mode) {
        return mode != null && (mode != Mode.HERBICIDE || ModConfigs.SPRINKLER_HERBICIDE.get());
    }

    // One spray every cycle on the server
    public static void tick(KineticSprinklerBlockEntity sprinkler) {
        if (!(sprinkler.getLevel() instanceof ServerLevel level) || level.getGameTime() % ModConfigs.SPRINKLER_CYCLE_TICKS.get() != 0) {
            return;
        }
        FluidTank tank = sprinkler.fluidTank;
        Mode mode = mode(tank.getFluid());
        boolean wasRunning = running(level, sprinkler);
        int sprayed = active(mode) ? tank.drain(ModConfigs.SPRINKLER_FLUID_PER_CYCLE.get(), IFluidHandler.FluidAction.EXECUTE).getAmount() : 0;
        if (sprayed > 0 && mode.irrigates) {
            sprinkler.setData(ModAttachments.SPRINKLER_DRY_SINCE, -1L);
        } else if (sprinkler.getData(ModAttachments.SPRINKLER_DRY_SINCE) < 0) {
            sprinkler.setData(ModAttachments.SPRINKLER_DRY_SINCE, level.getGameTime());
            sprinkler.setChanged();
        }
        if (sprayed <= 0) return;
        int r = radius(mode);
        float perBlock = sprayed / (float) ((2 * r + 1) * (2 * r + 1));
        BlockPos origin = sprinkler.getBlockPos();
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                BlockPos landing = sprayColumn(level, origin, origin.offset(dx, 0, dz), mode, perBlock, wasRunning);
                if (landing != null && level.random.nextFloat() < 0.7f) {
                    level.sendParticles(mode.particle, landing.getX() + 0.5, landing.getY() + 1.1, landing.getZ() + 0.5,
                            2, 0.3, 0.05, 0.3, 0.0);
                }
            }
        }
        sprinkler.setChanged();
        sprinkler.sendData();
    }

    // Sprays down one column until soil or a solid block; returns where the spray landed
    private static BlockPos sprayColumn(ServerLevel level, BlockPos origin, BlockPos top, Mode mode, float mb, boolean wasRunning) {
        int depth = ModConfigs.SPRINKLER_DEPTH.get();
        for (int dy = 0; dy >= -depth; dy--) {
            BlockPos pos = top.offset(0, dy, 0);
            if (pos.equals(origin)) continue;
            BlockState state = level.getBlockState(pos);
            if (mode == Mode.HERBICIDE && poison(level, pos, state)) continue;
            BlockEntity be = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
            if (be instanceof IFarmland soil) {
                if (mode.irrigates) water(level, origin, pos, be, wasRunning);
                feed(soil, mode, mb);
                return pos;
            }
            if (be instanceof ClimateReceiver) {
                if (mode.irrigates) water(level, origin, pos, be, wasRunning);
                return pos;
            }
            if (!state.isAir() && state.isCollisionShapeFullBlock(level, pos)) {
                return pos;
            }
        }
        return null;
    }

    // Irrigating unless dry for longer than the soil stays moist; an unloaded chunk or a night's sleep doesn't count as dry
    public static boolean running(Level level, KineticSprinklerBlockEntity sprinkler) {
        long drySince = sprinkler.getData(ModAttachments.SPRINKLER_DRY_SINCE);
        return drySince < 0 || level.getGameTime() - drySince <= ModConfigs.SPRINKLER_RETENTION_TICKS.get();
    }

    public static boolean running(Level level, BlockPos pos) {
        return level.isLoaded(pos) && level.getBlockEntity(pos) instanceof KineticSprinklerBlockEntity sprinkler
                && sprinkler.hasData(ModAttachments.SPRINKLER_DRY_SINCE) && running(level, sprinkler);
    }

    private static void water(ServerLevel level, BlockPos origin, BlockPos pos, BlockEntity be, boolean wasRunning) {
        if (be instanceof FarmlandBlockEntity farmland) {
            Irrigation.stamp(level, farmland, origin, wasRunning);
        } else if (be instanceof ClimateReceiver planter) {
            planter.addWater(ModConfigs.SPRINKLER_PLANTER_WATER.get().floatValue(), pos.getY() < origin.getY() ? Direction.UP : Direction.DOWN);
        }
    }

    private static void feed(IFarmland soil, Mode mode, float mb) {
        List<? extends Double> npk = switch (mode) {
            case FERTILIZER -> ModConfigs.SPRINKLER_FERTILIZER.get();
            case RICH_FERTILIZER -> ModConfigs.SPRINKLER_RICH_FERTILIZER.get();
            default -> null;
        };
        if (npk == null || npk.size() < 3) return;
        NutrientType[] types = {NutrientType.NITROGEN, NutrientType.PHOSPHOROUS, NutrientType.POTASSIUM};
        for (int i = 0; i < 3; i++) {
            float dose = (float) (npk.get(i) * mb / 1000.0);
            if (dose > 0) soil.addNutrient(types[i], dose);
        }
    }

    // Herbicide: crops die, wild plants and grass cover go, grass blocks turn to their dirt; true to keep going down
    private static boolean poison(ServerLevel level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof ICropBlock crop) {
            if (!(block instanceof DoubleCropBlock) || state.getValue(DoubleCropBlock.PART) == DoubleCropBlock.Part.BOTTOM) {
                crop.die(level, pos, state, false);
            }
            return true;
        }
        if (block instanceof DeadCropBlock) return true;
        if (block instanceof TFCBushBlock || (block instanceof BushBlock && state.is(BlockTags.REPLACEABLE_BY_TREES))) {
            level.destroyBlock(pos, false);
            return true;
        }
        if (block instanceof ConnectedGrassBlock grass) {
            level.setBlockAndUpdate(pos, grass.getDirt());
        } else if (state.is(Blocks.GRASS_BLOCK)) {
            level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
        }
        return false;
    }

    // Whether an irrigating sprinkler above covers this soil, for client overlays
    public static boolean irrigatedBySprinkler(Level level, BlockPos soil) {
        int reach = ModConfigs.SPRINKLER_RADIUS.get() + ModConfigs.SPRINKLER_RICH_BONUS.get();
        int depth = ModConfigs.SPRINKLER_DEPTH.get();
        for (int dy = 0; dy <= depth; dy++) {
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    if (level.getBlockEntity(soil.offset(dx, dy, dz)) instanceof KineticSprinklerBlockEntity sprinkler) {
                        Mode mode = mode(sprinkler.fluidTank.getFluid());
                        int r = mode == null ? -1 : radius(mode);
                        if (mode != null && mode.irrigates && Math.abs(dx) <= r && Math.abs(dz) <= r) return true;
                    }
                }
            }
        }
        return false;
    }

    public static void tooltip(KineticSprinklerBlockEntity sprinkler, List<Component> tooltip) {
        CreateLang.builder().add(Component.translatable("create.solar_punk.tooltip.sprinkler_header")).forGoggles(tooltip);
        FluidTank tank = sprinkler.fluidTank;
        Mode mode = mode(tank.getFluid());
        boolean running = active(mode) && tank.getFluidAmount() > 0;
        Component state = Component.translatable(running ? "materialismcore.sprinkler.mode." + mode.key()
                : mode == Mode.HERBICIDE ? "materialismcore.sprinkler.disabled" : "materialismcore.sprinkler.idle");
        CreateLang.builder().add(state).style(running ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY).forGoggles(tooltip, 1);
        if (running) {
            int side = 2 * radius(mode) + 1;
            CreateLang.builder().add(Component.translatable("materialismcore.sprinkler.area", side, side,
                    ModConfigs.SPRINKLER_DEPTH.get())).style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
            if (mode.irrigates) {
                CreateLang.builder().add(Component.translatable("materialismcore.sprinkler.hydration",
                        ModConfigs.SPRINKLER_HYDRATION_FLOOR.get())).style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
            }
        }
        Component fluid = tank.isEmpty() ? Component.translatable("materialismcore.vat.empty") : tank.getFluid().getHoverName();
        CreateLang.builder().add(Component.translatable("materialismcore.sprinkler.tank", fluid, tank.getFluidAmount(), tank.getCapacity()))
                .style(ChatFormatting.AQUA).forGoggles(tooltip, 1);
    }
}
