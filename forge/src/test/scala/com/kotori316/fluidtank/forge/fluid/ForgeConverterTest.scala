package com.kotori316.fluidtank.forge.fluid

import com.kotori316.fluidtank.fluids.FluidAmountUtil
import com.kotori316.fluidtank.forge.BeforeMC
import com.kotori316.fluidtank.forge.fluid.ForgeConverter._
import net.minecraft.world.level.material.Fluids
import net.minecraftforge.fluids.{FluidStack, FluidType}
import org.junit.jupiter.api.{Assertions, DynamicNode, DynamicTest, TestFactory}

class ForgeConverterTest extends BeforeMC {
  val conversion = Map(
    FluidAmountUtil.EMPTY -> FluidStack.EMPTY,
    FluidAmountUtil.BUCKET_WATER -> new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME),
    FluidAmountUtil.BUCKET_LAVA -> new FluidStack(Fluids.LAVA, FluidType.BUCKET_VOLUME),
  )

  @TestFactory
  def testToFluidStack(): Array[DynamicNode] = {
    conversion.map { case (amount, stack) => DynamicTest.dynamicTest(amount.toString, () => {
      Assertions.assertTrue(stack.isFluidStackIdentical(amount.toStack))
    })
    }.toArray
  }

  @TestFactory
  def testToFluidAmount(): Array[DynamicNode] = {
    conversion.map { case (amount, stack) => DynamicTest.dynamicTest(stack.toString, () => {
      Assertions.assertEquals(amount, stack.toAmount)
    })
    }.toArray
  }
}
