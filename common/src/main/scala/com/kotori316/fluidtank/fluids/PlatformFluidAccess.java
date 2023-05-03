package com.kotori316.fluidtank.fluids;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.fluidtank.contents.GenericAmount;
import com.kotori316.fluidtank.contents.GenericUnit;

public interface PlatformFluidAccess {
    @NotNull
    static PlatformFluidAccess getInstance() {
        return PlatformFluidAccessHolder.platformFluidAccess;
    }

    static void setInstance(@NotNull PlatformFluidAccess access) {
        PlatformFluidAccessHolder.platformFluidAccess = access;
    }

    boolean isGaseous(Fluid fluid);

    @NotNull
    Fluid getBucketContent(BucketItem bucketItem);

    @NotNull
    default GenericAmount<Fluid> getFluidContained(ItemStack stack) {
        if (stack.getItem() instanceof BucketItem bucketItem) {
            var fluid = getBucketContent(bucketItem);
            if (Fluids.EMPTY.equals(fluid)) {
                return FluidAmountUtil.EMPTY();
            }
            return FluidAmountUtil.from(fluid, GenericUnit.ONE_BUCKET());
        }
        return FluidAmountUtil.EMPTY();
    }

}

class PlatformFluidAccessHolder {
    @NotNull
    static PlatformFluidAccess platformFluidAccess = new Default();

    @ApiStatus.Internal
    private static final class Default implements PlatformFluidAccess {
        @Override
        public boolean isGaseous(Fluid fluid) {
            return false;
        }

        @Override
        public Fluid getBucketContent(BucketItem bucketItem) {
            try {
                var field = BucketItem.class.getDeclaredField("content");
                field.setAccessible(true);
                return (Fluid) field.get(bucketItem);
            } catch (ReflectiveOperationException e) {
                FluidTankCommon.LOGGER.error("Got error in getting fluid content of %s. Are you in production?".formatted(bucketItem), e);
                return Fluids.EMPTY;
            }
        }
    }
}
