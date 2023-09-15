package com.kotori316.fluidtank.fabric.reservoir

import com.kotori316.fluidtank.contents.Tank
import com.kotori316.fluidtank.fabric.FluidTank
import com.kotori316.fluidtank.fabric.fluid.FabricTankStorage
import com.kotori316.fluidtank.fluids.FluidLike
import com.kotori316.fluidtank.reservoir.ItemReservoir
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant

import scala.jdk.CollectionConverters.CollectionHasAsScala

//noinspection UnstableApiUsage
class ReservoirFluidStorage(c: ContainerItemContext) extends FabricTankStorage(c) {
  override def getTank: Tank[FluidLike] = {
    c.getItemVariant.getItem.asInstanceOf[ItemReservoir]
      .getTank(c.getItemVariant.toStack)
  }

  override def saveTank(newTank: Tank[FluidLike]): ItemVariant = {
    val stack = c.getItemVariant.toStack
    stack.getItem.asInstanceOf[ItemReservoir]
      .saveTank(stack, newTank)
    ItemVariant.of(stack)
  }
}

//noinspection UnstableApiUsage
object ReservoirFluidStorage {
  def register(): Unit = {
    FluidStorage.ITEM.registerForItems((_, context) => new ReservoirFluidStorage(context),
      FluidTank.RESERVOIR_MAP.values().asScala.toSeq *)
  }
}
