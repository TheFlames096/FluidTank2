package com.kotori316.fluidtank.item;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface PlatformItemAccess {
    static PlatformItemAccess getInstance() {
        return PlatformItemAccessHolder.access;
    }

    static void setInstance(PlatformItemAccess access) {
        PlatformItemAccessHolder.access = access;
    }

    @NotNull
    ItemStack getCraftingRemainingItem(ItemStack stack);
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
