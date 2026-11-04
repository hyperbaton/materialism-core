package com.pjbhva.materialismcore.compat;

import com.drmangotea.tfmg.recipes.VatMachineRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

import java.util.ArrayList;
import java.util.List;

/**
 * Optional fields the pack adds to TFMG's vat recipes. They all default to 0, which leaves the
 * recipe behaving as TFMG defines it. {@code min_current} and {@code energy} only apply to recipes
 * that use electrodes.
 *
 * @param electrodeWear durability each electrode the recipe uses loses per finished craft
 * @param minCurrent    amps each of those electrodes needs for the recipe to progress
 * @param energy        FE those electrodes must draw in total before the recipe can finish
 * @param temperature   degrees Celsius the vat must be at for the recipe to progress
 */
public record VatRecipeExtras(int electrodeWear, float minCurrent, int energy, float temperature) {
    public static final VatRecipeExtras NONE = new VatRecipeExtras(0, 0f, 0, 0f);

    private static final Codec<Float> NON_NEGATIVE_FLOAT = Codec.floatRange(0f, Float.MAX_VALUE);

    public static final MapCodec<VatRecipeExtras> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("electrode_wear", 0).forGetter(VatRecipeExtras::electrodeWear),
            NON_NEGATIVE_FLOAT.optionalFieldOf("min_current", 0f).forGetter(VatRecipeExtras::minCurrent),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("energy", 0).forGetter(VatRecipeExtras::energy),
            NON_NEGATIVE_FLOAT.optionalFieldOf("temperature", 0f).forGetter(VatRecipeExtras::temperature)
    ).apply(instance, VatRecipeExtras::new));

    public static final StreamCodec<ByteBuf, VatRecipeExtras> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, VatRecipeExtras::electrodeWear,
            ByteBufCodecs.FLOAT, VatRecipeExtras::minCurrent,
            ByteBufCodecs.VAR_INT, VatRecipeExtras::energy,
            ByteBufCodecs.FLOAT, VatRecipeExtras::temperature,
            VatRecipeExtras::new
    );

    public static VatRecipeExtras of(VatMachineRecipe recipe) {
        return ((Holder) recipe.getParams()).materialismcore$getExtras();
    }

    /** Short form of each requirement, for the JEI summary line. */
    public List<Component> requirements() {
        List<Component> requirements = new ArrayList<>();
        if (temperature > 0) {
            requirements.add(Component.translatable("materialismcore.vat.requirement.temperature", Math.round(temperature)));
        }
        if (minCurrent > 0) {
            requirements.add(Component.translatable("materialismcore.vat.requirement.current", Math.round(minCurrent)));
        }
        if (energy > 0) {
            requirements.add(Component.translatable("materialismcore.vat.requirement.energy", energy));
        }
        return requirements;
    }

    /** Full description of each field in use, for the JEI tooltip. */
    public List<Component> tooltip() {
        List<Component> tooltip = new ArrayList<>();
        if (temperature > 0) {
            tooltip.add(Component.translatable("materialismcore.vat.tooltip.temperature", Math.round(temperature)));
        }
        if (minCurrent > 0) {
            tooltip.add(Component.translatable("materialismcore.vat.tooltip.current", Math.round(minCurrent)));
        }
        if (energy > 0) {
            tooltip.add(Component.translatable("materialismcore.vat.tooltip.energy", energy));
        }
        if (electrodeWear > 0) {
            tooltip.add(Component.translatable("materialismcore.vat.tooltip.wear", electrodeWear));
        }
        return tooltip;
    }

    /** Implemented on {@code VatRecipeParams} by {@code VatRecipeParamsMixin}. */
    public interface Holder {
        VatRecipeExtras materialismcore$getExtras();

        void materialismcore$setExtras(VatRecipeExtras extras);
    }
}
