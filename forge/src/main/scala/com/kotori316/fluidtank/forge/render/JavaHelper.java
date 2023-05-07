package com.kotori316.fluidtank.forge.render;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;

final class JavaHelper {
    static FluidType getFluidType(Fluid fluid) {
        return fluid.getFluidType();
    }
}
