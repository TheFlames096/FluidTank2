package com.kotori316.fluidtank.fabric;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.mixin.transfer.BucketItemAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Option;

import com.kotori316.fluidtank.FluidTankCommon;
import com.kotori316.fluidtank.PlatformAccess;
import com.kotori316.fluidtank.contents.GenericAmount;
import com.kotori316.fluidtank.contents.GenericUnit;
import com.kotori316.fluidtank.fabric.fluid.FabricConverter;
import com.kotori316.fluidtank.fluids.FluidAmountUtil;
import com.kotori316.fluidtank.tank.TileTank;

@SuppressWarnings("UnstableApiUsage")
final class FabricPlatformAccess implements PlatformAccess {
    @Override
    public boolean isGaseous(Fluid fluid) {
        return FluidVariantAttributes.isLighterThanAir(FluidVariant.of(fluid));
    }

    @Override
    public @NotNull Fluid getBucketContent(BucketItem bucketItem) {
        return ((BucketItemAccessor) bucketItem).fabric_getFluid();
    }

    @Override
    public @NotNull GenericAmount<Fluid> getFluidContained(ItemStack stack) {
        var storage = FluidStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
        if (storage != null) {
            for (StorageView<FluidVariant> view : storage) {
                var variant = view.getResource();
                var amount = view.getAmount();
                return FluidAmountUtil.from(variant.getFluid(), GenericUnit.fromFabric(amount), Option.apply(variant.copyNbt()));
            }
        }
        return FluidAmountUtil.EMPTY();
    }

    @Override
    public boolean isFluidContainer(ItemStack stack) {
        var storage = FluidStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
        return storage != null;
    }

    @Override
    public Component getDisplayName(GenericAmount<Fluid> amount) {
        return FluidVariantAttributes.getName(FabricConverter.toVariant(amount));
    }

    @Override
    public @NotNull TransferStack fillItem(GenericAmount<Fluid> toFill, ItemStack stack, Player player, InteractionHand hand, boolean execute) {
        var context = ContainerItemContext.ofPlayerHand(player, hand);
        var storage = FluidStorage.ITEM.find(stack, context);
        if (storage == null) {
            return new TransferStack(FluidAmountUtil.EMPTY(), stack, false);
        }

        long filled;
        try (Transaction transaction = Transaction.openOuter()) {
            filled = storage.insert(FabricConverter.toVariant(toFill), FabricConverter.fabricAmount(toFill), transaction);
            if (execute) transaction.commit();
        }
        FluidTankCommon.LOGGER.warn("Fill context {} {} execute={}", context.getItemVariant(), context.getAmount(), execute);
        return new TransferStack(toFill.setAmount(GenericUnit.fromFabric(filled)), context.getItemVariant().toStack((int) context.getAmount()), false);
    }

    @Override
    public @NotNull TransferStack drainItem(GenericAmount<Fluid> toDrain, ItemStack stack, Player player, InteractionHand hand, boolean execute) {
        var context = ContainerItemContext.ofPlayerHand(player, hand);
        var storage = FluidStorage.ITEM.find(stack, context);
        if (storage == null) {
            return new TransferStack(FluidAmountUtil.EMPTY(), stack, false);
        }
        long drained;
        try (Transaction transaction = Transaction.openOuter()) {
            drained = storage.extract(FabricConverter.toVariant(toDrain), FabricConverter.fabricAmount(toDrain), transaction);
            if (execute) transaction.commit();
        }
        FluidTankCommon.LOGGER.warn("Drain context {} {} execute={}", context.getItemVariant(), context.getAmount(), execute);
        return new TransferStack(toDrain.setAmount(GenericUnit.fromFabric(drained)), context.getItemVariant().toStack((int) context.getAmount()), false);
    }

    @Override
    public @Nullable SoundEvent getEmptySound(GenericAmount<Fluid> fluid) {
        return FluidVariantAttributes.getEmptySound(FabricConverter.toVariant(fluid));
    }

    @Override
    public @Nullable SoundEvent getFillSound(GenericAmount<Fluid> fluid) {
        return FluidVariantAttributes.getFillSound(FabricConverter.toVariant(fluid));
    }

    @Override
    public BlockEntityType<? extends TileTank> getNormalType() {
        return FluidTank.TILE_TANK_TYPE;
    }

    @Override
    public BlockEntityType<? extends TileTank> getCreativeType() {
        return FluidTank.TILE_CREATIVE_TANK_TYPE;
    }

    @Override
    public BlockEntityType<? extends TileTank> getVoidType() {
        return FluidTank.TILE_VOID_TANK_TYPE;
    }
}
