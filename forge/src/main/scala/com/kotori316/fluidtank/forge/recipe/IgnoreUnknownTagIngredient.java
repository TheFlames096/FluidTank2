package com.kotori316.fluidtank.forge.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.kotori316.fluidtank.FluidTankCommon;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

// TODO use in tier recipe after forge implement custom ingredients
public final class IgnoreUnknownTagIngredient extends Ingredient {
    public static final Codec<Ingredient> SERIALIZER = new Serializer();

    private final List<? extends Value> values;

    public IgnoreUnknownTagIngredient(List<? extends Value> values) {
        super(values.stream());
        this.values = values;
    }

    public static IgnoreUnknownTagIngredient of(ItemLike item) {
        return new IgnoreUnknownTagIngredient(List.of(new ItemValue(new ItemStack(item))));
    }

    public static IgnoreUnknownTagIngredient of(TagKey<Item> tag) {
        return new IgnoreUnknownTagIngredient(List.of(new TagValue(tag)));
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static final class TagValue implements Value {
        private final TagKey<Item> tag;

        private TagValue(TagKey<Item> tag) {
            this.tag = tag;
        }

        @Override
        public Collection<ItemStack> getItems() {
            var manager = ForgeRegistries.ITEMS.tags();
            if (manager == null) {
                FluidTankCommon.LOGGER.warn("[IgnoreUnknownTagIngredient] Can't get items from tag {}", tag);
                return List.of();
            }
            return manager.getTag(this.tag).stream().map(ItemStack::new).toList();
        }

    }

    private static class Serializer implements Codec<Ingredient> {

        public IgnoreUnknownTagIngredient parse(JsonObject json) {
            List<Value> valueList;
            if (json.has("item") || json.has("tag")) {
                valueList = List.of(getValue(json));
            } else if (json.has("values")) {
                valueList = StreamSupport.stream(json.getAsJsonArray("values").spliterator(), false)
                    .map(JsonElement::getAsJsonObject)
                    .map(Serializer::getValue)
                    .toList();
            } else {
                throw new JsonParseException("An IgnoreUnknownTagIngredient entry needs either a tag, an item or an array");
            }
            return new IgnoreUnknownTagIngredient(valueList);
        }

        private static Value getValue(JsonObject json) {
            if (json.has("item")) {
                ItemStack stack = CraftingHelper.getItemStack(json, true, true);
                return new ItemValue(stack);
            } else if (json.has("tag")) {
                ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(json, "tag"));
                TagKey<Item> tagkey = TagKey.create(Registries.ITEM, resourcelocation);
                return new TagValue(tagkey);
            } else {
                throw new JsonParseException("An IgnoreUnknownTagIngredient entry needs either a tag or an item");
            }
        }

        @Override
        public <T> DataResult<Pair<Ingredient, T>> decode(DynamicOps<T> ops, T input) {
            var json = ops.convertTo(JsonOps.INSTANCE, input);
            if (!json.isJsonObject()) {
                return DataResult.error(() -> "%s is not map. It can't be loaded as a recipe".formatted(input));
            } else {
                return DataResult.success(Pair.of(parse(json.getAsJsonObject()), ops.empty()));
            }
        }

        @Override
        public <T> DataResult<T> encode(Ingredient ingredient, DynamicOps<T> ops, T prefix) {

            try {
                // Run in dev environment only
                var field = Ingredient.class.getDeclaredField("values");
                field.setAccessible(true);
                var values = (Ingredient.Value[]) field.get(ingredient);

                if (values.length == 1) {
                    return encodeValue(values[0], ops, prefix);
                } else {
                    var list = ops.listBuilder();
                    Stream.of(values).map(v -> encodeValue(v, ops, ops.empty()))
                        .forEach(list::add);
                    return list.build(prefix);
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        private static <T> DataResult<T> encodeValue(Value value, DynamicOps<T> ops, T prefix) {
            var builder = ops.mapBuilder()
                .add("type", ops.createString(FluidTankCommon.modId + ":ignore_unknown_tag_ingredient"));
            if (value instanceof Ingredient.ItemValue || value instanceof Ingredient.TagValue) {
                return builder.build(Value.CODEC.encode(value, ops, prefix));
            } else if (value instanceof IgnoreUnknownTagIngredient.TagValue tagValue) {
                return builder
                    .add("tag", ops.createString(tagValue.tag.location().toString()))
                    .build(prefix);
            } else {
                return DataResult.error(() -> "Unexpected value type " + value);
            }
        }
    }
}
