package com.kotori316.fluidtank.fabric.tank;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Test;

import com.kotori316.fluidtank.contents.Tank;
import com.kotori316.fluidtank.contents.TankUtil;
import com.kotori316.fluidtank.fabric.BeforeMC;
import com.kotori316.fluidtank.fabric.FluidTank;
import com.kotori316.fluidtank.fluids.FluidAmountUtil;
import com.kotori316.fluidtank.tank.Tier;
import com.kotori316.fluidtank.tank.TileTank;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("UnstableApiUsage")
final class FabricTankItemStorageTest extends BeforeMC {
    @Test
    void instance() {
        SingleSlotStorage<FluidVariant> storage = new FabricTankItemStorage(ContainerItemContext.withConstant(new ItemStack(FluidTank.TANK_MAP.get(Tier.WOOD))));
        assertNotNull(storage);
        assertAll(
            () -> assertTrue(storage.isResourceBlank()),
            () -> assertEquals(0, storage.getAmount()),
            () -> assertEquals(4 * FluidConstants.BUCKET, storage.getCapacity())
        );
    }
    @Test
    void initialState1() {
        var stack = new ItemStack(FluidTank.TANK_MAP.get(Tier.WOOD));
        var tag = stack.getOrCreateTagElement(BlockItem.BLOCK_ENTITY_TAG);
        tag.putString(TileTank.KEY_TIER(), Tier.WOOD.name());
        var tank = Tank.apply(FluidAmountUtil.BUCKET_WATER(), Tier.WOOD.getCapacity());
        tag.put(TileTank.KEY_TANK(), TankUtil.save(tank, FluidAmountUtil.access()));

        var storage = new FabricTankItemStorage(ContainerItemContext.withConstant(stack));
        assertAll(
            () -> assertEquals(FluidConstants.BUCKET, storage.getAmount()),
            () -> assertEquals(FluidVariant.of(Fluids.WATER), storage.getResource()),
            () -> assertEquals(4 * FluidConstants.BUCKET, storage.getCapacity())
        );
    }

}
