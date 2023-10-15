package com.kotori316.fluidtank.forge.recipe;

import com.google.gson.JsonObject;
import com.kotori316.fluidtank.recipe.TierRecipe;
import com.kotori316.fluidtank.tank.Tier;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.IShapedRecipe;
import org.jetbrains.annotations.Nullable;

public final class TierRecipeForge extends TierRecipe implements IShapedRecipe<CraftingContainer> {
    public static final RecipeSerializer<TierRecipe> SERIALIZER = new Serializer();

    public TierRecipeForge(Tier tier, Ingredient tankItem, Ingredient subItem) {
        super(tier, tankItem, subItem);
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
        public Serializer() {
            // This codec includes forge magic!
            super(Ingredient.CODEC_NONEMPTY);
        }

        @Override
        protected TierRecipe createInstance(Tier tier, Ingredient tankItem, Ingredient subItem) {
            return new TierRecipeForge(tier, tankItem, subItem);
        }

        /*@Override
        public TierRecipe fromJson(ResourceLocation recipeLoc, JsonObject recipeJson, ICondition.IContext context) {
            return super.fromJson(recipeLoc, recipeJson, context);
        }*/
    }

    public static class TierFinishedRecipe implements FinishedRecipe {
        private final ResourceLocation recipeId;
        private final Tier tier;
        private final Ingredient subIngredient;

        public TierFinishedRecipe(ResourceLocation recipeId, Tier tier, Ingredient subIngredient) {
            this.recipeId = recipeId;
            this.tier = tier;
            this.subIngredient = subIngredient;
        }

        @Override
        public void serializeRecipeData(JsonObject object) {
            // In serialization, tank ingredient is unused
            var recipe = new TierRecipeForge(tier, Ingredient.EMPTY, subIngredient);
            var recipeJson = ((Serializer) SERIALIZER).toJson(recipe);
            recipeJson.entrySet().forEach(e ->
                object.add(e.getKey(), e.getValue())
            );
        }

        @Override
        public ResourceLocation id() {
            return recipeId;
        }

        @Override
        public RecipeSerializer<?> type() {
            return SERIALIZER;
        }

        @Nullable
        @Override
        public AdvancementHolder advancement() {
            return null;
        }
    }
}
