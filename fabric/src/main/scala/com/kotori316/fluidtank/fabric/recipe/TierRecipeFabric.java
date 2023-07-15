package com.kotori316.fluidtank.fabric.recipe;

import com.kotori316.fluidtank.recipe.TierRecipe;
import com.kotori316.fluidtank.tank.Tier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class TierRecipeFabric extends TierRecipe {
    public static final RecipeSerializer<TierRecipe> SERIALIZER = new Serializer();

    public TierRecipeFabric(ResourceLocation id, Tier tier, Ingredient tankItem, Ingredient subItem) {
        super(id, tier, tankItem, subItem);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static class Serializer extends SerializerBase {
        @Override
        protected TierRecipe createInstance(ResourceLocation id, Tier tier, Ingredient tankItem, Ingredient subItem) {
            return new TierRecipeFabric(id, tier, tankItem, subItem);
        }
    }
}
