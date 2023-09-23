package com.kotori316.fluidtank.fabric.recipe;

import com.kotori316.fluidtank.recipe.TierRecipe;
import com.kotori316.fluidtank.tank.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class TierRecipeFabric extends TierRecipe {
    public static final TierRecipe.SerializerBase SERIALIZER = new Serializer();

    public TierRecipeFabric(Tier tier, Ingredient tankItem, Ingredient subItem) {
        super(tier, tankItem, subItem);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static class Serializer extends SerializerBase {
        public Serializer() {
            super(Ingredient.CODEC_NONEMPTY);
        }

        @Override
        protected TierRecipe createInstance(Tier tier, Ingredient tankItem, Ingredient subItem) {
            return new TierRecipeFabric(tier, tankItem, subItem);
        }
    }
}
