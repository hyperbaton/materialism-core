package com.pjbhva.materialismcore.compat.jei;

import com.pjbhva.materialismcore.MaterialismCore;
import com.pjbhva.materialismcore.registry.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

@JeiPlugin
public class MaterialismJeiPlugin implements IModPlugin {
    private static final ResourceLocation VAT = ResourceLocation.fromNamespaceAndPath("solarpunk", "fermentation_vat");
    private static final ResourceLocation SPRINKLER = ResourceLocation.fromNamespaceAndPath("solarpunk", "kinetic_sprinkler");

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MaterialismCore.MOD_ID, "jei_plugin");
    }

    private static ItemStack vat() {
        return new ItemStack(BuiltInRegistries.ITEM.get(VAT));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new FermentingCategory(registration.getJeiHelpers().getGuiHelper(), vat()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (Minecraft.getInstance().level != null) {
            registration.addRecipes(FermentingCategory.TYPE,
                    Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(ModRecipes.FERMENTING.get()));
        }
        if (ModList.get().isLoaded("solarpunk")) {
            ItemStack sprinkler = new ItemStack(BuiltInRegistries.ITEM.get(SPRINKLER));
            registration.addIngredientInfo(sprinkler, VanillaTypes.ITEM_STACK,
                    Component.translatable("materialismcore.jei.sprinkler.1"), Component.translatable("materialismcore.jei.sprinkler.2"),
                    Component.translatable("materialismcore.jei.sprinkler.3"), Component.translatable("materialismcore.jei.sprinkler.4"));
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        if (ModList.get().isLoaded("solarpunk")) {
            registration.addRecipeCatalyst(vat(), FermentingCategory.TYPE);
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        if (ModList.get().isLoaded("solarpunk")) {
            runtime.getRecipeManager().hideRecipeCategory(net.succ.solar_punk.compat.jei.FermentationVatCategory.RECIPE_TYPE);
        }
    }
}
