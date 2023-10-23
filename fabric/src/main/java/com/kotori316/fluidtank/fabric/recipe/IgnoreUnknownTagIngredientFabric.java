package com.kotori316.fluidtank.fabric.recipe;

import com.kotori316.fluidtank.FluidTankCommon;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.AnyIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public final class IgnoreUnknownTagIngredientFabric extends AnyIngredient {
    public static final ResourceLocation NAME = new ResourceLocation(FluidTankCommon.modId, "ignore_unknown_tag_ingredient");
    public static final CustomIngredientSerializer<IgnoreUnknownTagIngredientFabric> SERIALIZER = new Serializer();

    public IgnoreUnknownTagIngredientFabric(List<Ingredient> bases) {
        super(bases);
    }

    IgnoreUnknownTagIngredientFabric(Ingredient ingredient) {
        this(List.of(ingredient));
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    List<Ingredient> getBase() {
        return this.ingredients;
    }

    static final class Serializer implements CustomIngredientSerializer<IgnoreUnknownTagIngredientFabric> {
        static final Codec<IgnoreUnknownTagIngredientFabric> ALLOW_EMPTY = createCodec(true);
        static final Codec<IgnoreUnknownTagIngredientFabric> NON_EMPTY = createCodec(false);

        @Override
        public ResourceLocation getIdentifier() {
            return NAME;
        }

        @Override
        public Codec<IgnoreUnknownTagIngredientFabric> getCodec(boolean allowEmpty) {
            return allowEmpty ? ALLOW_EMPTY : NON_EMPTY;
        }

        @Override
        public IgnoreUnknownTagIngredientFabric read(FriendlyByteBuf buf) {
            var base = buf.readCollection(ArrayList::new, Ingredient::fromNetwork);
            return new IgnoreUnknownTagIngredientFabric(base);
        }

        @Override
        public void write(FriendlyByteBuf buf, IgnoreUnknownTagIngredientFabric ingredient) {
            buf.writeCollection(ingredient.getBase(), (b, i) -> i.toNetwork(b));
        }

        static Codec<IgnoreUnknownTagIngredientFabric> createCodec(boolean allowEmpty) {
            var base = allowEmpty ? Ingredient.CODEC : Ingredient.CODEC_NONEMPTY;

            var values = base.listOf().fieldOf("values")
                .xmap(IgnoreUnknownTagIngredientFabric::new, IgnoreUnknownTagIngredientFabric::getBase);
            return values.codec();
        }

    }
}
