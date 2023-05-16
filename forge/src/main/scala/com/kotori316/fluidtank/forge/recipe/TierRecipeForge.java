package com.kotori316.fluidtank.forge.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.common.crafting.conditions.ICondition;

import com.kotori316.fluidtank.recipe.TierRecipe;
import com.kotori316.fluidtank.tank.Tier;

public final class TierRecipeForge extends TierRecipe implements IShapedRecipe<CraftingContainer> {
    public static final RecipeSerializer<TierRecipe> SERIALIZER = new Serializer();

    public TierRecipeForge(ResourceLocation id, Tier tier, Ingredient tankItem, Ingredient subItem) {
        super(id, tier, tankItem, subItem);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public int getRecipeWidth() {
        return 3;
    }

    @Override
    public int getRecipeHeight() {
        return 3;
    }

    public static class Serializer extends SerializerBase {
        @Override
        protected TierRecipe createInstance(ResourceLocation id, Tier tier, Ingredient tankItem, Ingredient subItem) {
            return new TierRecipeForge(id, tier, tankItem, subItem);
        }

        @Override
        public TierRecipe fromJson(ResourceLocation recipeLoc, JsonObject recipeJson, ICondition.IContext context) {
            return super.fromJson(recipeLoc, recipeJson, context);
        }
    }
}
