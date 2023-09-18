package com.kotori316.fluidtank.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PlatformItemAccess {
    static PlatformItemAccess getInstance() {
        return PlatformItemAccessHolder.access;
    }

    static void setInstance(PlatformItemAccess access) {
        PlatformItemAccessHolder.access = access;
    }

    @NotNull
    ItemStack getCraftingRemainingItem(ItemStack stack);

    static void setTileTag(@NotNull ItemStack stack, @Nullable CompoundTag tileTag) {
        if (tileTag == null || tileTag.isEmpty()) {
            stack.removeTagKey(BlockItem.BLOCK_ENTITY_TAG);
        } else {
            stack.addTagElement(BlockItem.BLOCK_ENTITY_TAG, tileTag);
        }
    }

    static String convertIngredientToString(Ingredient ingredient) {
        return "[%s]".formatted(ingredient.toJson(true));
    }

}

class PlatformItemAccessHolder {
    @NotNull
    static PlatformItemAccess access = new Default();


    private static class Default implements PlatformItemAccess {

        @Override
        public @NotNull ItemStack getCraftingRemainingItem(ItemStack stack) {
            var remaining = stack.getItem().getCraftingRemainingItem();
            if (remaining == null) return ItemStack.EMPTY;
            else return remaining.getDefaultInstance();
        }
    }
}
