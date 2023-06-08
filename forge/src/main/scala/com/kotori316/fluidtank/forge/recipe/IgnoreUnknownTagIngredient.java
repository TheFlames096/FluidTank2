package com.kotori316.fluidtank.forge.recipe;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.VanillaIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import com.kotori316.fluidtank.FluidTankCommon;

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
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", Objects.requireNonNull(CraftingHelper.getID(SERIALIZER)).toString());
        if (this.values.size() == 1) {
            var value = this.values.get(0).serialize();
            value.entrySet().forEach(e -> json.add(e.getKey(), e.getValue()));
            return json;
        }
        var values = this.values.stream().map(Value::serialize)
            .reduce(new JsonArray(), (a, object) -> {
                a.add(object);
                return a;
            }, (a1, a2) -> {
                a1.addAll(a2);
                return a1;
            });
        json.add("values", values);
        return json;
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

        @Override
        public JsonObject serialize() {
            JsonObject json = new JsonObject();
            json.addProperty("tag", this.tag.location().toString());
            return json;
        }
    }

    private static class Serializer implements IIngredientSerializer<IgnoreUnknownTagIngredient> {

        @Override
        public IgnoreUnknownTagIngredient parse(FriendlyByteBuf buffer) {
            return new IgnoreUnknownTagIngredient(Stream.generate(() -> new Ingredient.ItemValue(buffer.readItem())).limit(buffer.readVarInt()).toList());
        }

        @Override
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
                Item item = ShapedRecipe.itemFromJson(json);
                return new ItemValue(new ItemStack(item));
            } else if (json.has("tag")) {
                ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(json, "tag"));
                TagKey<Item> tagkey = TagKey.create(Registries.ITEM, resourcelocation);
                return new TagValue(tagkey);
            } else {
                throw new JsonParseException("An IgnoreUnknownTagIngredient entry needs either a tag or an item");
            }
        }

        @Override
        public void write(FriendlyByteBuf arg, IgnoreUnknownTagIngredient arg2) {
            VanillaIngredientSerializer.INSTANCE.write(arg, arg2);
        }
    }
}
