package com.kotori316.fluidtank.forge.render;

import com.kotori316.fluidtank.contents.GenericAmount;
import com.kotori316.fluidtank.fluids.FluidLike;
import com.kotori316.fluidtank.fluids.VanillaFluid;
import com.kotori316.fluidtank.fluids.VanillaPotion;
import com.kotori316.fluidtank.forge.fluid.ForgeConverter;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;

final class JavaHelper {
    static FluidType getFluidType(Fluid fluid) {
        return fluid.getFluidType();
    }

    static int getLightLevel(GenericAmount<FluidLike> amount) {
        if (amount.content() instanceof VanillaFluid vanillaFluid) {
            return getFluidType(vanillaFluid.fluid()).getLightLevel(ForgeConverter.toStack(amount));
        } else if (amount.content() instanceof VanillaPotion) {
            return 0;
        } else {
            throw new AssertionError();
        }
    }
}
