package com.pjbhva.materialismcore.registry;

import com.pjbhva.materialismcore.MaterialismCore;
import com.pjbhva.materialismcore.recipe.FermentingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, MaterialismCore.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MaterialismCore.MOD_ID);

    public static final Supplier<RecipeType<FermentingRecipe>> FERMENTING = RECIPE_TYPES.register("fermenting",
            () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(MaterialismCore.MOD_ID, "fermenting")));
    public static final Supplier<RecipeSerializer<FermentingRecipe>> FERMENTING_SERIALIZER =
            RECIPE_SERIALIZERS.register("fermenting", FermentingRecipe.Serializer::new);
}
