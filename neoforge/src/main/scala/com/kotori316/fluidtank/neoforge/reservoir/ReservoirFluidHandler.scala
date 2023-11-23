package com.kotori316.fluidtank.neoforge.reservoir

import com.kotori316.fluidtank.contents.Tank
import com.kotori316.fluidtank.fluids.FluidLike
import com.kotori316.fluidtank.neoforge.fluid.TankFluidHandler
import com.kotori316.fluidtank.reservoir.ItemReservoir
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.common.capabilities.{Capabilities, Capability, ICapabilityProvider}
import net.neoforged.neoforge.common.util.LazyOptional
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem

class ReservoirFluidHandler(reservoir: ItemReservoir, stack: ItemStack) extends TankFluidHandler with ICapabilityProvider {
  private val handler = LazyOptional.of[IFluidHandlerItem](() => this)

  override def getContainer: ItemStack = stack

  override def getCapability[T](capability: Capability[T], arg: Direction): LazyOptional[T] =
    Capabilities.FLUID_HANDLER_ITEM.orEmpty(capability, this.handler)

  override def getTank: Tank[FluidLike] = reservoir.getTank(stack)

  override def saveTank(newTank: Tank[FluidLike]): Unit = reservoir.saveTank(stack, newTank)
}
