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
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Option;
import scala.jdk.javaapi.OptionConverters;

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
    default GenericAmount<FluidLike> getFluidContained(ItemStack stack) {
        if (stack.getItem() instanceof BucketItem bucketItem) {
            var fluid = getBucketContent(bucketItem);
            if (Fluids.EMPTY.equals(fluid)) {
                return FluidAmountUtil.EMPTY();
            }
            return FluidAmountUtil.from(fluid, GenericUnit.ONE_BUCKET());
        } else if (stack.getItem() instanceof PotionItem potionItem) {
            var potionFluid = FluidLike.of(PotionType.fromItemUnsafe(potionItem));
            return FluidAmountUtil.from(potionFluid, GenericUnit.ONE_BOTTLE(), Option.apply(stack.getTag()));
        }
        return FluidAmountUtil.EMPTY();
    }

    boolean isFluidContainer(ItemStack stack);

    Component getDisplayName(GenericAmount<FluidLike> amount);

    /**
     * @param execute used in fabric, where item transfer will be done in fabric context. In forge, this param has no meaning.
     * @return the filled amount and filled stack
     */
    @NotNull
    TransferStack fillItem(GenericAmount<FluidLike> toFill, ItemStack fluidContainer, Player player, InteractionHand hand, boolean execute);

    /**
     * @param execute used in fabric, where item transfer will be done in fabric context. In forge, this param has no meaning.
     * @return the drained amount and drained stack
     */
    @NotNull
    TransferStack drainItem(GenericAmount<FluidLike> toDrain, ItemStack fluidContainer, Player player, InteractionHand hand, boolean execute);

    @Nullable SoundEvent getEmptySound(GenericAmount<FluidLike> fluid);

    @Nullable SoundEvent getFillSound(GenericAmount<FluidLike> fluid);

    /**
     * The result of transferring fluids.
     */
    final class TransferStack {
        private final GenericAmount<FluidLike> moved;
        private final ItemStack toReplace;
        private final boolean shouldMove;

        /**
         * @param moved      filled or drained amount
         * @param toReplace  the result item with transferred fluids
         * @param shouldMove whether to move {@code toReplace} item into player inventory. In fabric {@code false} and in forge {@code true}.
         */
        public TransferStack(GenericAmount<FluidLike> moved, ItemStack toReplace, boolean shouldMove) {
            this.moved = moved;
            this.toReplace = toReplace;
            this.shouldMove = shouldMove;
        }

        /**
         * Helper constructor for forge.
         */
        public TransferStack(GenericAmount<FluidLike> moved, ItemStack toReplace) {
            this(moved, toReplace, true);
        }

        public GenericAmount<FluidLike> moved() {
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
        public Component getDisplayName(GenericAmount<FluidLike> amount) {
            return Component.literal(FluidAmountUtil.access().asString(amount.content()));
        }

        @Override
        public TransferStack fillItem(GenericAmount<FluidLike> toFill, ItemStack fluidContainer, Player player, InteractionHand hand, boolean execute) {
            if (toFill.content() instanceof VanillaFluid vanillaFluid) {
                // Just for vanilla bucket
                if (fluidContainer.getItem() == Items.BUCKET && toFill.hasOneBucket()) {
                    var filledItem = vanillaFluid.fluid().getBucket().getDefaultInstance();
                    var filledAmount = toFill.setAmount(GenericUnit.ONE_BUCKET());
                    return new TransferStack(filledAmount, filledItem);
                }
                return new TransferStack(FluidAmountUtil.EMPTY(), fluidContainer, false);
            } else if (toFill.content() instanceof VanillaPotion vanillaPotion) {
                if (fluidContainer.is(Items.GLASS_BOTTLE) && toFill.hasOneBottle()) {
                    var filledItem = PotionUtils.setPotion(
                            new ItemStack(vanillaPotion.potionType().getItem()),
                            OptionConverters.toJava(toFill.nbt()).map(PotionUtils::getPotion).orElse(Potions.EMPTY)
                    );
                    var filledAmount = toFill.setAmount(GenericUnit.ONE_BOTTLE());
                    return new TransferStack(filledAmount, filledItem);
                }
                return new TransferStack(FluidAmountUtil.EMPTY(), fluidContainer, false);
            } else {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public TransferStack drainItem(GenericAmount<FluidLike> toDrain, ItemStack fluidContainer, Player player, InteractionHand hand, boolean execute) {
            var bucketFluid = getFluidContained(fluidContainer);
            if (!toDrain.hasOneBucket() || !toDrain.contentEqual(bucketFluid)) {
                // Nothing drained
                return new TransferStack(FluidAmountUtil.EMPTY(), fluidContainer, false);
            }
            var drainedItem = Items.BUCKET.getDefaultInstance();
            var drainedAmount = toDrain.setAmount(GenericUnit.ONE_BUCKET());
            return new TransferStack(drainedAmount, drainedItem);
        }

        @Override
        @SuppressWarnings("deprecation")
        public SoundEvent getEmptySound(GenericAmount<FluidLike> fluid) {
            return FluidLike.asFluid(fluid.content(), Fluids.WATER).is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        }

        @Override
        @Nullable
        public SoundEvent getFillSound(GenericAmount<FluidLike> fluid) {
            return FluidLike.asFluid(fluid.content(), Fluids.WATER).getPickupSound().orElse(null);
        }
    }
}
