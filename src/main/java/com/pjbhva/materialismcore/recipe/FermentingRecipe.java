package com.pjbhva.materialismcore.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pjbhva.materialismcore.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Optional;

/**
 * A batch in the Solarpunk fermentation vat ({@code materialismcore:fermenting}).
 * Amounts are per batch; a vat runs one batch per 16 blocks of volume (the minimum vat is 2x2x4).
 *
 * <pre>{
 *   "type": "materialismcore:fermenting",
 *   "ingredient": {"item": "minecraft:sugar", "count": 3},
 *   "fluid_ingredient": {"fluid": "firmalife:yeast_starter", "amount": 100},
 *   "result": {"id": "chemica:ethanol", "amount": 1000},
 *   "processing_time": 800
 * }</pre>
 */
public record FermentingRecipe(Optional<SizedIngredient> ingredient, SizedFluidIngredient fluidIngredient,
                               FluidStack result, int processingTime) implements Recipe<FermentingInput> {
    public static final MapCodec<FermentingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SizedIngredient.FLAT_CODEC.optionalFieldOf("ingredient").forGetter(FermentingRecipe::ingredient),
            SizedFluidIngredient.FLAT_CODEC.fieldOf("fluid_ingredient").forGetter(FermentingRecipe::fluidIngredient),
            FluidStack.CODEC.fieldOf("result").forGetter(FermentingRecipe::result),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("processing_time", 400).forGetter(FermentingRecipe::processingTime)
    ).apply(instance, FermentingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FermentingRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(SizedIngredient.STREAM_CODEC), FermentingRecipe::ingredient,
            SizedFluidIngredient.STREAM_CODEC, FermentingRecipe::fluidIngredient,
            FluidStack.STREAM_CODEC, FermentingRecipe::result,
            ByteBufCodecs.VAR_INT, FermentingRecipe::processingTime,
            FermentingRecipe::new);

    @Override
    public boolean matches(FermentingInput input, Level level) {
        boolean itemMatches = ingredient.map(sized -> sized.ingredient().test(input.item())).orElse(true);
        return itemMatches && fluidIngredient.ingredient().test(input.fluid());
    }

    @Override
    public ItemStack assemble(FermentingInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FERMENTING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.FERMENTING.get();
    }

    public static class Serializer implements RecipeSerializer<FermentingRecipe> {
        @Override
        public MapCodec<FermentingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FermentingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
