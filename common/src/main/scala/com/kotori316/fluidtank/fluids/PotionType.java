package com.kotori316.fluidtank.fluids;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;

public enum PotionType {
    NORMAL((PotionItem) Items.POTION),
    SPLASH((PotionItem) Items.SPLASH_POTION),
    LINGERING((PotionItem) Items.LINGERING_POTION);

    private final PotionItem item;

    PotionType(PotionItem item) {
        this.item = item;
    }

    public PotionItem getItem() {
        return item;
    }
}
