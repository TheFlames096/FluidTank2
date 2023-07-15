package com.kotori316.fluidtank.fluids;

import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.fluidtank.contents.GenericAmount;
import com.kotori316.fluidtank.contents.GenericUnit;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * @param execute used in fabric, where item transfer will be done in fabric context. In forge, this param has no meaning.
     * @return the filled amount and filled stack
     */
    @NotNull
    TransferStack fillItem(GenericAmount<Fluid> toFill, ItemStack fluidContainer, Player player, InteractionHand hand, boolean execute);

    /**
     * @param execute used in fabric, where item transfer will be done in fabric context. In forge, this param has no meaning.
     * @return the drained amount and drained stack
     */
    @NotNull
    TransferStack drainItem(GenericAmount<Fluid> toDrain, ItemStack fluidContainer, Player player, InteractionHand hand, boolean execute);

    @Nullable SoundEvent getEmptySound(GenericAmount<Fluid> fluid);

    @Nullable SoundEvent getFillSound(GenericAmount<Fluid> fluid);

    /**
     * The result of transferring fluids.
     */
    final class TransferStack {
        private final GenericAmount<Fluid> moved;
        private final ItemStack toReplace;
        private final boolean shouldMove;

        /**
         * @param moved      filled or drained amount
         * @param toReplace  the result item with transferred fluids
         * @param shouldMove whether to move {@code toReplace} item into player inventory. In fabric {@code false} and in forge {@code true}.
         */
        public TransferStack(GenericAmount<Fluid> moved, ItemStack toReplace, boolean shouldMove) {
            this.moved = moved;
            this.toReplace = toReplace;
            this.shouldMove = shouldMove;
        }

        /**
         * Helper constructor for forge.
         */
        public TransferStack(GenericAmount<Fluid> moved, ItemStack toReplace) {
            this(moved, toReplace, true);
        }

        public GenericAmount<Fluid> moved() {
            return moved;
        }

        public ItemStack toReplace() {
            return toReplace;
        }

        public boolean shouldMove() {
            return shouldMove;
        }

        @Override
        public String toString() {
            return "TransferStack[" +
                    "moved=" + moved + ", " +
                    "toReplace=" + toReplace + ", " +
                    "shouldMove=" + shouldMove + ']';
        }
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

        @Override
        public boolean isFluidContainer(ItemStack stack) {
            return stack.getItem() instanceof BucketItem;
        }

        @Override
        public Component getDisplayName(GenericAmount<Fluid> amount) {
            return Component.literal(FluidAmountUtil.access().asString(amount.content()));
        }

        @Override
        public TransferStack fillItem(GenericAmount<Fluid> toFill, ItemStack fluidContainer, Player player, InteractionHand hand, boolean execute) {
            // Just for vanilla bucket
            if (fluidContainer.getItem() == Items.BUCKET && toFill.hasOneBucket()) {
                var filledItem = toFill.content().getBucket().getDefaultInstance();
                var filledAmount = toFill.setAmount(GenericUnit.ONE_BUCKET());
                return new TransferStack(filledAmount, filledItem);
            }
            return new TransferStack(FluidAmountUtil.EMPTY(), fluidContainer);
        }

        @Override
        public TransferStack drainItem(GenericAmount<Fluid> toDrain, ItemStack fluidContainer, Player player, InteractionHand hand, boolean execute) {
            var bucketFluid = getFluidContained(fluidContainer);
            if (!toDrain.hasOneBucket() || !toDrain.contentEqual(bucketFluid)) {
                // Nothing drained
                return new TransferStack(FluidAmountUtil.EMPTY(), fluidContainer);
            }
            var drainedItem = Items.BUCKET.getDefaultInstance();
            var drainedAmount = toDrain.setAmount(GenericUnit.ONE_BUCKET());
            return new TransferStack(drainedAmount, drainedItem);
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
