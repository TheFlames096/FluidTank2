package com.kotori316.fluidtank.fluids;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    boolean isFluidContainer(ItemStack stack);

    Component getDisplayName(GenericAmount<Fluid> amount);

    /**
     * @return the filled amount and filled stack
     */
    @NotNull
    Pair<GenericAmount<Fluid>, ItemStack> fillItem(GenericAmount<Fluid> toFill, ItemStack fluidContainer);

    /**
     * @return the drained amount and drained stack
     */
    @NotNull
    Pair<GenericAmount<Fluid>, ItemStack> drainItem(GenericAmount<Fluid> toDrain, ItemStack fluidContainer);

    @Nullable SoundEvent getEmptySound(GenericAmount<Fluid> fluid);

    @Nullable SoundEvent getFillSound(GenericAmount<Fluid> fluid);
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

        @Override
        public boolean isFluidContainer(ItemStack stack) {
            return stack.getItem() instanceof BucketItem;
        }

        @Override
        public Component getDisplayName(GenericAmount<Fluid> amount) {
            return Component.literal(FluidAmountUtil.access().asString(amount.content()));
        }

        @Override
        public Pair<GenericAmount<Fluid>, ItemStack> fillItem(GenericAmount<Fluid> toFill, ItemStack fluidContainer) {
            // Just for vanilla bucket
            if (fluidContainer.getItem() == Items.BUCKET && toFill.hasOneBucket()) {
                var filledItem = toFill.content().getBucket().getDefaultInstance();
                var filledAmount = toFill.setAmount(GenericUnit.ONE_BUCKET());
                return Pair.of(filledAmount, filledItem);
            }
            return Pair.of(FluidAmountUtil.EMPTY(), fluidContainer);
        }

        @Override
        public Pair<GenericAmount<Fluid>, ItemStack> drainItem(GenericAmount<Fluid> toDrain, ItemStack fluidContainer) {
            var bucketFluid = getFluidContained(fluidContainer);
            if (!toDrain.hasOneBucket() || !toDrain.contentEqual(bucketFluid)) {
                // Nothing drained
                return Pair.of(FluidAmountUtil.EMPTY(), fluidContainer);
            }
            var drainedItem = Items.BUCKET.getDefaultInstance();
            var drainedAmount = toDrain.setAmount(GenericUnit.ONE_BUCKET());
            return Pair.of(drainedAmount, drainedItem);
        }

        @Override
        @SuppressWarnings("deprecation")
        public SoundEvent getEmptySound(GenericAmount<Fluid> fluid) {
            return fluid.content().is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        }

        @Override
        @Nullable
        public SoundEvent getFillSound(GenericAmount<Fluid> fluid) {
            return fluid.content().getPickupSound().orElse(null);
        }
    }
}
