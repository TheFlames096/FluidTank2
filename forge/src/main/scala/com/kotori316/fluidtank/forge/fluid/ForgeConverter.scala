package com.kotori316.fluidtank.forge.fluid

import com.kotori316.fluidtank.contents.GenericUnit
import com.kotori316.fluidtank.fluids.{FluidAmount, FluidAmountUtil}
import net.minecraftforge.fluids.FluidStack

object ForgeConverter {
  def toStack(amount: FluidAmount): FluidStack = {
    new FluidStack(amount.content, amount.amount.asForge, amount.nbt.orNull)
  }

  def toAmount(stack: FluidStack): FluidAmount = {
    FluidAmountUtil.from(stack.getRawFluid, GenericUnit.fromForge(stack.getAmount), Option(stack.getTag))
  }

  implicit final class FluidAmount2FluidStack(private val a: FluidAmount) extends AnyVal {
    def toStack: FluidStack = ForgeConverter.toStack(a)
  }

  implicit final class FluidStack2FluidAmount(private val stack: FluidStack) extends AnyVal {
    def toAmount: FluidAmount = ForgeConverter.toAmount(stack)
  }
}
