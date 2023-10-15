package com.kotori316.fluidtank.forge.recipe;

import com.google.gson.JsonArray;
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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.ingredients.AbstractIngredient;
import net.minecraftforge.common.crafting.ingredients.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import scala.jdk.javaapi.CollectionConverters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.StreamSupport;

public final class IgnoreUnknownTagIngredient extends AbstractIngredient {
    public static final IIngredientSerializer<IgnoreUnknownTagIngredient> SERIALIZER = new Serializer();

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

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> serializer() {
        return SERIALIZER;
    }

    public scala.collection.Seq<? extends Value> getValues() {
        return CollectionConverters.asScala(this.values);
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

    private static class Serializer implements IIngredientSerializer<IgnoreUnknownTagIngredient> {
        private static final Codec<IgnoreUnknownTagIngredient> CODEC = new C();

        @Override
        public Codec<? extends IgnoreUnknownTagIngredient> codec() {
            return CODEC;
        }

        @Override
        public void write(FriendlyByteBuf buffer, IgnoreUnknownTagIngredient ingredient) {
            buffer.writeCollection(List.of(ingredient.getItems()), FriendlyByteBuf::writeItem);
        }

        @Override
        public IgnoreUnknownTagIngredient read(FriendlyByteBuf buffer) {
            var items = buffer.readCollection(ArrayList::new, FriendlyByteBuf::readItem);
            var values = items.stream().map(ItemValue::new).toList();
            return new IgnoreUnknownTagIngredient(values);
        }
    }

    private static class C implements Codec<IgnoreUnknownTagIngredient> {

        public IgnoreUnknownTagIngredient parse(JsonObject json) {
            if (json.has("item") || json.has("tag")) {
                List<Value> valueList = List.of(getValue(json));
                return new IgnoreUnknownTagIngredient(valueList);
            } else if (json.has("values")) {
                return parse(json.getAsJsonArray("values"));
            } else {
                throw new JsonParseException("An IgnoreUnknownTagIngredient entry needs either a tag, an item or an array");
            }
        }

        public IgnoreUnknownTagIngredient parse(JsonArray json) {
            List<Value> valueList = StreamSupport.stream(json.spliterator(), false)
                .map(JsonElement::getAsJsonObject)
                .map(C::getValue)
                .toList();
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
        public <T> DataResult<Pair<IgnoreUnknownTagIngredient, T>> decode(DynamicOps<T> ops, T input) {
            var json = ops.convertTo(JsonOps.INSTANCE, input);
            if (json.isJsonObject()) {
                return DataResult.success(Pair.of(parse(json.getAsJsonObject()), ops.empty()));
            } else if (json.isJsonArray()) {
                return DataResult.success(Pair.of(parse(json.getAsJsonArray()), ops.empty()));
            } else {
                return DataResult.error(() -> "%s is not map. It can't be loaded as a recipe".formatted(input));
            }
        }

        @Override
        public <T> DataResult<T> encode(IgnoreUnknownTagIngredient ingredient, DynamicOps<T> ops, T prefix) {
            var values = ingredient.values;

            if (values.size() == 1) {
                return encodeValue(values.get(0), ops, prefix);
            } else {
                var list = ops.listBuilder();
                values.stream().map(v -> encodeValue(v, ops, ops.empty()))
                    .forEach(list::add);
                return list.build(prefix);
            }
        }

        private static <T> DataResult<T> encodeValue(Value value, DynamicOps<T> ops, T prefix) {
            var builder = ops.mapBuilder();
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
