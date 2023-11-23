package com.kotori316.fluidtank.neoforge.fluid

import com.kotori316.fluidtank.contents.{DefaultTransferEnv, GenericUnit, Tank}
import com.kotori316.fluidtank.fluids.{FluidAmount, FluidLike}
import com.kotori316.fluidtank.neoforge.fluid.NeoForgeConverter.*
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.capability.{IFluidHandler, IFluidHandlerItem}

trait TankFluidHandler extends IFluidHandlerItem {
  def getTank: Tank[FluidLike]

  def saveTank(newTank: Tank[FluidLike]): Unit

  override def getTanks: Int = 1

  override def getTankCapacity(i: Int): Int = getTank.capacity.asForge

  override def getFluidInTank(i: Int): FluidStack = getTank.content.toStack

  override def isFluidValid(i: Int, fluidStack: FluidStack): Boolean = true

  override def fill(fluidStack: FluidStack, fluidAction: IFluidHandler.FluidAction): Int = {
    val op = getTank.fillOp
    val (_, rest, tank) = op.run(DefaultTransferEnv, fluidStack.toAmount)
    if (fluidAction.execute()) saveTank(tank)
    fluidStack.getAmount - rest.amount.asForge
  }

  override def drain(drain: FluidStack, fluidAction: IFluidHandler.FluidAction): FluidStack = {
    val tank = getTank
    if (tank.isEmpty) {
      FluidStack.EMPTY
    } else {
      drainInternal(tank, drain.toAmount, fluidAction)
    }
  }

  override def drain(amount: Int, fluidAction: IFluidHandler.FluidAction): FluidStack = {
    val tank = getTank
    if (tank.isEmpty) {
      FluidStack.EMPTY
    } else {
      drainInternal(tank, tank.content.setAmount(GenericUnit.fromForge(amount)), fluidAction)
    }
  }

  private def drainInternal(tank: Tank[FluidLike], drainAmount: FluidAmount, fluidAction: IFluidHandler.FluidAction): FluidStack = {
    val (_, rest, newTank) = tank.drainOp.run(DefaultTransferEnv, drainAmount)
    if (fluidAction.execute()) saveTank(newTank)
    (drainAmount - rest).toStack
  }

}
