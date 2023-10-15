package com.kotori316.fluidtank.forge.recipe;

import com.google.gson.JsonObject;
import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.fluidtank.forge.BeforeMC;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class IgnoreUnknownTagIngredientTest extends BeforeMC {
    static void assertIngredientEqual(Ingredient expected, Ingredient actual) {
        if (Objects.equals(expected, actual))
            return;
        var e = expected.getItems();
        var a = actual.getItems();
        if (e.length != a.length) {
            fail("The length of ingredient doesn't match");
        }
        for (int i = 0; i < e.length; i++) {
            var e1 = e[i];
            var a1 = a[i];
            assertTrue(ItemStack.matches(e1, a1), "Item at %d".formatted(i));
        }
    }

    @Nested
    class VanillaCheck {
        @Test
        void vanillaItemIngredient() {
            // language=json
            var json = """
                {
                  "item": "minecraft:apple"
                }
                """;
            var result = Ingredient.VANILLA_CODEC.decode(JsonOps.INSTANCE, GsonHelper.parse(json));
            var ingredient = assertDoesNotThrow(() -> result.get().orThrow().getFirst());
            var expected = Ingredient.of(Items.APPLE);

            assertIngredientEqual(expected, ingredient);
        }

        @Test
        void vanillaTagIngredient() {
            // language=json
            var json = """
                {
                  "tag": "minecraft:logs"
                }
                """;
            var result = Ingredient.VANILLA_CODEC.decode(JsonOps.INSTANCE, GsonHelper.parse(json));
            var ingredient = assertDoesNotThrow(() -> result.get().orThrow().getFirst());
            var expected = Ingredient.of(ItemTags.LOGS);

            assertIngredientEqual(expected, ingredient);
        }

        @Test
        void vanillaUnknownTag() {
            // language=json
            var json = """
                {
                  "tag": "c:logs"
                }
                """;
            var result = Ingredient.CODEC.decode(JsonOps.INSTANCE, GsonHelper.parse(json));
            var ingredient = assertDoesNotThrow(() -> result.get().orThrow().getFirst());
            assertNotEquals(0, ingredient.getItems().length);
        }
    }

    @Nested
    class DeserializeTest {

        @Test
        void customItemIngredient() {
            // language=json
            var json = """
                {
                  "type": "fluidtank:ignore_unknown_tag_ingredient",
                  "item": "minecraft:apple"
                }
                """;
            var result = Ingredient.CODEC.decode(JsonOps.INSTANCE, GsonHelper.parse(json));
            var ingredient = assertInstanceOf(IgnoreUnknownTagIngredient.class, assertDoesNotThrow(() -> result.get().orThrow().getFirst()));
            var expected = IgnoreUnknownTagIngredient.of(Items.APPLE);

            assertIngredientEqual(expected, ingredient);
        }

        @Test
        void customTagIngredient() {
            // language=json
            var json = """
                {
                  "type": "fluidtank:ignore_unknown_tag_ingredient",
                  "tag": "minecraft:logs"
                }
                """;
            var result = Ingredient.CODEC.decode(JsonOps.INSTANCE, GsonHelper.parse(json));
            var ingredient = assertInstanceOf(IgnoreUnknownTagIngredient.class, assertDoesNotThrow(() -> result.get().orThrow().getFirst()));
            var expected = IgnoreUnknownTagIngredient.of(ItemTags.LOGS);

            assertIngredientEqual(expected, ingredient);
        }

        @Test
        void customUnknownTag() {
            // language=json
            var json = """
                {
                  "type": "fluidtank:ignore_unknown_tag_ingredient",
                  "tag": "c:logs"
                }
                """;
            var result = Ingredient.CODEC.decode(JsonOps.INSTANCE, GsonHelper.parse(json));
            var ingredient = assertInstanceOf(IgnoreUnknownTagIngredient.class, assertDoesNotThrow(() -> result.get().orThrow().getFirst()));
            assertEquals(0, ingredient.getItems().length);
        }
    }

    @SuppressWarnings("unchecked")
    @Nested
    class SerializeTest {
        @Test
        void singleItemInternal() {
            var ingredient = IgnoreUnknownTagIngredient.of(Items.APPLE);
            var codec = (Codec<IgnoreUnknownTagIngredient>) IgnoreUnknownTagIngredient.SERIALIZER.codec();
            var encoded = codec.encodeStart(JsonOps.INSTANCE, ingredient);
            var json = assertDoesNotThrow(() -> encoded.get().orThrow());
            assertTrue(json.isJsonObject());

            var expected = new JsonObject();
            expected.addProperty("item", "minecraft:apple");
            assertEquals(expected, json);
        }

        @Test
        void singleItemWithType() {
            var ingredient = IgnoreUnknownTagIngredient.of(Items.APPLE);
            var encoded = Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ingredient);
            var json = assertDoesNotThrow(() -> encoded.get().orThrow());
            assertTrue(json.isJsonObject());

            var expected = new JsonObject();
            expected.addProperty("type", FluidTankCommon.modId + ":ignore_unknown_tag_ingredient");
            expected.addProperty("item", "minecraft:apple");
            assertEquals(expected, json);
        }

        @Test
        void singleTag() {
            var ingredient = IgnoreUnknownTagIngredient.of(ItemTags.LOGS);
            var encoded = Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ingredient);
            var json = assertDoesNotThrow(() -> encoded.get().orThrow());
            assertTrue(json.isJsonObject());

            var expected = new JsonObject();
            expected.addProperty("type", FluidTankCommon.modId + ":ignore_unknown_tag_ingredient");
            expected.addProperty("tag", "minecraft:logs");
            assertEquals(expected, json);
        }
    }
}
