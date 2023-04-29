package com.kotori316.fluidtank.fluids

import com.kotori316.fluidtank.BeforeMC
import com.kotori316.fluidtank.contents.GenericUnit
import net.minecraft.world.item.{ItemStack, Items}
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.{DynamicNode, DynamicTest, Nested, Test, TestFactory}


class FluidAmountUtilTest extends BeforeMC {

  @Test
  def waterBucket(): Unit = {
    val water = FluidAmountUtil.BUCKET_WATER
    assertEquals(GenericUnit.ONE_BUCKET, water.amount)
    assertFalse(water.isEmpty)
  }

  @TestFactory
  def empty(): Array[DynamicNode] = {
    Seq(
      DynamicTest.dynamicTest("empty fluid", () => assertTrue(FluidAmountUtil.EMPTY.isEmpty)),
      DynamicTest.dynamicTest("amount 1000, empty fluid", () => assertTrue(FluidAmountUtil.EMPTY.setAmount(GenericUnit.ONE_BUCKET).isEmpty)),
      DynamicTest.dynamicTest("amount 0, lava", () => assertTrue(FluidAmountUtil.BUCKET_LAVA.setAmount(GenericUnit.ZERO).isEmpty)),
      DynamicTest.dynamicTest("amount 0, water", () => assertTrue(FluidAmountUtil.BUCKET_WATER.setAmount(GenericUnit.ZERO).isEmpty)),
    ).toArray
  }

  @TestFactory
  def cycle(): Array[DynamicNode] = {
    val fluids = Seq(
      FluidAmountUtil.EMPTY,
      FluidAmountUtil.BUCKET_WATER,
      FluidAmountUtil.BUCKET_LAVA,
    )
    fluids.map(f => DynamicTest.dynamicTest(s"cycle $f", () => {
      val tag = f.getTag
      val reconstructed = FluidAmountUtil.fromTag(tag)
      assertEquals(f, reconstructed)
    })).toArray
  }

  @Nested
  class FromItemTest {
    @Test
    def fromEmptyBucket(): Unit = {
      val fluid = FluidAmountUtil.fromItem(new ItemStack(Items.BUCKET))
      assertEquals(FluidAmountUtil.EMPTY, fluid)
    }

    @Test
    def fromWaterBucket(): Unit = {
      val fluid = FluidAmountUtil.fromItem(new ItemStack(Items.WATER_BUCKET))
      assertEquals(FluidAmountUtil.BUCKET_WATER, fluid)
    }

    @Test
    def fromLavaBucket(): Unit = {
      val fluid = FluidAmountUtil.fromItem(new ItemStack(Items.LAVA_BUCKET))
      assertEquals(FluidAmountUtil.BUCKET_LAVA, fluid)
    }
  }
}
