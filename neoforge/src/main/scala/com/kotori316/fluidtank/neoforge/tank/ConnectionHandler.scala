package com.kotori316.fluidtank.neoforge.tank

import com.kotori316.fluidtank.contents.GenericUnit
import com.kotori316.fluidtank.fluids.{FluidAmountUtil, FluidConnection}
import com.kotori316.fluidtank.neoforge.fluid.NeoForgeConverter.*
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.capability.IFluidHandler

class ConnectionHandler(connection: FluidConnection) extends IFluidHandler {
  override def getTanks: Int = 1

  override def getFluidInTank(i: Int): FluidStack = connection.getContent.map(_.toStack).getOrElse(FluidStack.EMPTY)

  override def getTankCapacity(i: Int): Int = connection.capacity.asForge

  override def isFluidValid(i: Int, fluidStack: FluidStack): Boolean = true

  override def fill(fluidStack: FluidStack, fluidAction: IFluidHandler.FluidAction): Int = {
    val filled = connection.getHandler.fill(fluidStack.toAmount, fluidAction.execute())
    filled.amount.asForge
  }

  override def drain(fluidStack: FluidStack, fluidAction: IFluidHandler.FluidAction): FluidStack = {
    val drained = connection.getHandler.drain(fluidStack.toAmount, fluidAction.execute())
    drained.toStack
  }

  override def drain(amount: Int, fluidAction: IFluidHandler.FluidAction): FluidStack = {
    val toDrain = connection.getContent.map(_.setAmount(GenericUnit.fromForge(amount))).getOrElse(FluidAmountUtil.EMPTY)
    val drained = connection.getHandler.drain(toDrain, fluidAction.execute())
    drained.toStack
  }
}
