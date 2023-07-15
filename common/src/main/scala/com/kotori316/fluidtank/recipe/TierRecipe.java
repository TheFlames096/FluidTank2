package com.kotori316.fluidtank.recipe;

import com.google.gson.JsonObject;
import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.fluidtank.contents.GenericAmount;
import com.kotori316.fluidtank.contents.GenericUnit;
import com.kotori316.fluidtank.contents.Tank;
import com.kotori316.fluidtank.contents.TankUtil;
import com.kotori316.fluidtank.fluids.FluidAmountUtil;
import com.kotori316.fluidtank.fluids.FluidKey;
import com.kotori316.fluidtank.item.PlatformItemAccess;
import com.kotori316.fluidtank.tank.ItemBlockTank;
import com.kotori316.fluidtank.tank.PlatformTankAccess;
import com.kotori316.fluidtank.tank.Tier;
import com.kotori316.fluidtank.tank.TileTank;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class TierRecipe implements CraftingRecipe {
    private static final Logger LOGGER = LoggerFactory.getLogger(TierRecipe.class);

    private final ResourceLocation id;
    private final Tier tier;
    private final Ingredient tankItem;
    private final Ingredient subItem;
    private final ItemStack result;
    private static final int recipeWidth = 3;
    private static final int recipeHeight = 3;

    protected TierRecipe(ResourceLocation id, Tier tier, Ingredient tankItem, Ingredient subItem) {
        this.id = id;
        this.tier = tier;
        this.tankItem = tankItem;
        this.subItem = subItem;
        this.result = new ItemStack(PlatformTankAccess.getInstance().getTankBlockMap().get(tier).get());

        LOGGER.debug("{} instance({}) created for Tier {}({}).", getClass().getSimpleName(), id, tier, result);
    }

    @Override
    public boolean matches(CraftingContainer inv, @Nullable Level worldIn) {
        return checkInv(inv);
    }

    private boolean checkInv(CraftingContainer inv) {
        for (int i = 0; i <= inv.getWidth() - recipeWidth; ++i) {
            for (int j = 0; j <= inv.getHeight() - recipeHeight; ++j) {
                if (this.checkMatch(inv, i, j)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the region of a crafting inventory is match for the recipe.
     * <p>Copied from {@link net.minecraft.world.item.crafting.ShapedRecipe}</p>
     */
    public boolean checkMatch(CraftingContainer craftingInventory, int w, int h) {
        NonNullList<Ingredient> ingredients = this.getIngredients();
        for (int i = 0; i < craftingInventory.getWidth(); ++i) {
            for (int j = 0; j < craftingInventory.getHeight(); ++j) {
                int k = i - w;
                int l = j - h;
                Ingredient ingredient;
                if (k >= 0 && l >= 0 && k < recipeWidth && l < recipeHeight) {
                    ingredient = ingredients.get(recipeWidth - k - 1 + l * recipeWidth);
                } else {
                    ingredient = Ingredient.EMPTY;
                }

                if (!ingredient.test(craftingInventory.getItem(i + j * craftingInventory.getWidth()))) {
                    return false;
                }
            }
        }

        // Items are placed correctly.
        List<ItemStack> tankStacks = IntStream.range(0, craftingInventory.getContainerSize())
                .mapToObj(craftingInventory::getItem)
                .filter(this.tankItem)
                .toList();
        return tankStacks.size() == 4 &&
                tankStacks.stream().map(BlockItem::getBlockEntityData)
                        .filter(Objects::nonNull)
                        .map(nbt -> TankUtil.load(nbt.getCompound(TileTank.KEY_TANK()), FluidAmountUtil.access()))
                        .map(Tank::content)
                        .filter(GenericAmount::nonEmpty)
                        .map(FluidKey::from)
                        .distinct()
                        .count() <= 1;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess access) {
        if (!this.checkInv(inv)) {
            LOGGER.error("Requested to return crafting result for invalid inventory. {}",
                    IntStream.range(0, inv.getContainerSize()).mapToObj(inv::getItem).collect(Collectors.toList()));
            return ItemStack.EMPTY;
        }
        ItemStack result = getResultItem(access);
        GenericAmount<Fluid> fluidAmount = IntStream.range(0, inv.getContainerSize()).mapToObj(inv::getItem)
                .filter(s -> s.getItem() instanceof ItemBlockTank)
                .map(BlockItem::getBlockEntityData)
                .filter(Objects::nonNull)
                .map(nbt -> TankUtil.load(nbt.getCompound(TileTank.KEY_TANK()), FluidAmountUtil.access()))
                .map(Tank::content)
                .filter(GenericAmount::nonEmpty)
                .reduce(GenericAmount::add).orElse(FluidAmountUtil.EMPTY());

        if (fluidAmount.nonEmpty()) {
            CompoundTag compound = new CompoundTag();

            var tank = new Tank<>(fluidAmount, GenericUnit.apply(tier.getCapacity()));
            CompoundTag tankTag = TankUtil.save(tank, FluidAmountUtil.access());
            compound.put(TileTank.KEY_TANK(), tankTag);
            compound.putString(TileTank.KEY_TIER(), tier.name());

            PlatformItemAccess.setTileTag(result, compound);
        }

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return result.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(tankItem); // 0
        ingredients.add(subItem); // 1
        ingredients.add(tankItem); // 2
        ingredients.add(subItem); // 3
        ingredients.add(Ingredient.EMPTY); // 4
        ingredients.add(subItem); // 5
        ingredients.add(tankItem); // 6
        ingredients.add(subItem); // 7
        ingredients.add(tankItem); // 8
        return ingredients;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        return IntStream.range(0, inv.getContainerSize())
                .mapToObj(inv::getItem)
                .map(stack -> {
                    if (stack.getItem() instanceof ItemBlockTank) return ItemStack.EMPTY;
                    else return PlatformItemAccess.getInstance().getCraftingRemainingItem(stack);
                })
                .collect(Collectors.toCollection(NonNullList::create));
    }

    public Tier getTier() {
        return tier;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    public static final String KEY_TIER = "tier";
    public static final String KEY_SUB_ITEM = "sub_item";

    public static abstract class SerializerBase implements RecipeSerializer<TierRecipe> {
        public static final ResourceLocation LOCATION = new ResourceLocation(FluidTankCommon.modId, "crafting_grade_up");

        protected abstract TierRecipe createInstance(ResourceLocation id, Tier tier, Ingredient tankItem, Ingredient subItem);

        @Override
        public TierRecipe fromJson(ResourceLocation recipeId, JsonObject serializedRecipe) {
            Tier tier = Tier.valueOf(GsonHelper.getAsString(serializedRecipe, KEY_TIER).toUpperCase(Locale.ROOT));
            Ingredient tankItem = getIngredientTankForTier(tier);
            Ingredient subItem = Ingredient.fromJson(serializedRecipe.get(KEY_SUB_ITEM));
            if (subItem == Ingredient.EMPTY)
                LOGGER.warn("Empty ingredient was loaded for {}, data: {}", recipeId, serializedRecipe);
            LOGGER.debug("Serializer loaded {} from json for tier {}, sub {}.", recipeId, tier, PlatformItemAccess.convertIngredientToString(subItem));
            return createInstance(recipeId, tier, tankItem, subItem);
        }

        public void toJson(JsonObject object, TierRecipe recipe) {
            object.addProperty(KEY_TIER, recipe.tier.name());
            object.add(KEY_SUB_ITEM, recipe.subItem.toJson());
        }

        @Override
        public TierRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            String tierName = buffer.readUtf();
            Tier tier = Tier.valueOf(tierName);
            Ingredient tankItem = Ingredient.fromNetwork(buffer);
            Ingredient subItem = Ingredient.fromNetwork(buffer);
            if (subItem == Ingredient.EMPTY)
                LOGGER.warn("Empty ingredient was loaded for {}", recipeId);
            LOGGER.debug("Serializer loaded {} from packet for tier {}, sub {}.", recipeId, tier, PlatformItemAccess.convertIngredientToString(subItem));
            return createInstance(recipeId, tier, tankItem, subItem);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, TierRecipe recipe) {
            buffer.writeUtf(recipe.tier.name());
            recipe.tankItem.toNetwork(buffer);
            recipe.subItem.toNetwork(buffer);
            LOGGER.debug("Serialized {} to packet for tier {}.", recipe.id, recipe.tier);
        }

        @VisibleForTesting
        public static Ingredient getIngredientTankForTier(Tier tier) {
            var targetTiers = Stream.of(Tier.values()).filter(t -> t.getRank() == tier.getRank() - 1);
            var itemStream = targetTiers.map(PlatformTankAccess.getInstance().getTankBlockMap()::get).map(Supplier::get).map(ItemStack::new);
            return Ingredient.of(itemStream);
        }
    }
}
