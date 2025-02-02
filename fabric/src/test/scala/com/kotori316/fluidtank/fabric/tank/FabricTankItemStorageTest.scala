package com.kotori316.fluidtank.fabric.tank

import com.kotori316.fluidtank.contents.{GenericUnit, Tank, TankUtil}
import com.kotori316.fluidtank.fabric.{BeforeMC, FluidTank}
import com.kotori316.fluidtank.fluids.{FluidAmountUtil, fluidAccess}
import com.kotori316.fluidtank.tank.{Tier, TileTank}
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.{FluidConstants, FluidVariant}
import net.minecraft.world.item.{BlockItem, ItemStack, Items}
import net.minecraft.world.level.material.Fluids
import org.junit.jupiter.api.Assertions.{assertAll, assertEquals, assertNotNull, assertNull, assertTrue}
import org.junit.jupiter.api.{Nested, Test}

@SuppressWarnings(Array("UnstableApiUsage"))
//noinspection UnstableApiUsage
final class FabricTankItemStorageTest extends BeforeMC {
  @Test
  def instance(): Unit = {
    val storage = new FabricTankItemStorage(ContainerItemContext.withConstant(new ItemStack(FluidTank.TANK_MAP.get(Tier.WOOD))))
    assertNotNull(storage)
    assertAll(
      () => assertTrue(storage.isResourceBlank),
      () => assertEquals(0, storage.getAmount),
      () => assertEquals(4 * FluidConstants.BUCKET, storage.getCapacity)
    )
  }

  @Test
  def initialState1(): Unit = {
    val stack = new ItemStack(FluidTank.TANK_MAP.get(Tier.WOOD))
    val tag = stack.getOrCreateTagElement(BlockItem.BLOCK_ENTITY_TAG)
    tag.putString(TileTank.KEY_TIER, Tier.WOOD.name)
    val tank = Tank(FluidAmountUtil.BUCKET_WATER, GenericUnit(Tier.WOOD.getCapacity))
    tag.put(TileTank.KEY_TANK, TankUtil.save(tank))
    val storage = new FabricTankItemStorage(ContainerItemContext.withConstant(stack))
    assertAll(
      () => assertEquals(FluidConstants.BUCKET, storage.getAmount),
      () => assertEquals(FluidVariant.of(Fluids.WATER), storage.getResource),
      () => assertEquals(4 * FluidConstants.BUCKET, storage.getCapacity),
      () => assertEquals(tank, storage.getTank),
    )
  }
}
