package com.kotori316.fluidtank.tank;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.Locale;

/**
 * Do not relay on this property for logic.
 * The property may be modified with Debug Stick, so this property should be used for visual purpose only.
 */
public enum TankPos implements StringRepresentable, Comparable<TankPos> {
    TOP, MIDDLE, BOTTOM, SINGLE;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static final EnumProperty<TankPos> TANK_POS_PROPERTY = EnumProperty.create("tank_pos", TankPos.class);
}
