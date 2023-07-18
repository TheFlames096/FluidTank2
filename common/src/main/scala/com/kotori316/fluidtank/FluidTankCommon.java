package com.kotori316.fluidtank;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public final class FluidTankCommon {
    public static final String modId = "fluidtank";
    public static final Logger LOGGER = LoggerFactory.getLogger(FluidTankCommon.class);
    public static final Marker MARKER_CONNECTION = MarkerFactory.getMarker("Connection");
    public static final Marker MARKER_TANK = MarkerFactory.getMarker("Tank");
    public static final Marker MARKER_FLUID_LIKE = MarkerFactory.getMarker("FluidLike");

    private static final double d = 1 / 16d;
    public static final VoxelShape TANK_SHAPE = Shapes.create(new AABB(2 * d, 0, 2 * d, 14 * d, 1d, 14 * d));
}
