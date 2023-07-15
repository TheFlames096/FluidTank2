package com.kotori316.fluidtank.tank;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.Locale;

public enum TankPos implements StringRepresentable, Comparable<TankPos> {
    TOP, MIDDLE, BOTTOM, SINGLE;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static final EnumProperty<TankPos> TANK_POS_PROPERTY = EnumProperty.create("tank_pos", TankPos.class);
}
