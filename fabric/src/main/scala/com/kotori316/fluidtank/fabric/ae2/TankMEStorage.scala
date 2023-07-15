package com.kotori316.fluidtank.fabric.ae2

import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.api.stacks.{AEFluidKey, AEItemKey, AEKey, KeyCounter}
import appeng.api.storage.MEStorage
import cats.implicits.catsSyntaxEq
import com.kotori316.fluidtank.MCImplicits.*
import com.kotori316.fluidtank.contents.GenericUnit
import com.kotori316.fluidtank.fluids.{FluidAmount, FluidAmountUtil}
import com.kotori316.fluidtank.tank.TileTank
import net.minecraft.network.chat.Component

case class TankMEStorage(tank: TileTank) extends MEStorage {

  override def getDescription: Component = tank.getName

  override def isPreferredStorageFor(what: AEKey, source: IActionSource): Boolean = {
    what match
      case key: AEFluidKey =>
        tank.getConnection.getContent.forall { c =>
          c.content === key.getFluid && c.nbt === Option(key.getTag)
        }
      case _ => false
  }

  override def insert(what: AEKey, amount: Long, mode: Actionable, source: IActionSource): Long = {
    MEStorage.checkPreconditions(what, amount, mode, source)
    what match
      case key: AEFluidKey =>
        val filled = this.tank.getConnection.getHandler.fill(fromAeFluid(key, amount), mode == Actionable.MODULATE)
        filled.amount.asForge
      case _ => 0
  }

  override def extract(what: AEKey, amount: Long, mode: Actionable, source: IActionSource): Long = {
    MEStorage.checkPreconditions(what, amount, mode, source)
    what match
      case key: AEFluidKey =>
        val drained = this.tank.getConnection.getHandler.drain(fromAeFluid(key, amount), mode == Actionable.MODULATE)
        drained.amount.asForge
      case _ => 0
  }

  override def getAvailableStacks(out: KeyCounter): Unit = {
    this.tank.getConnection.getContent.foreach { c =>
      out.add(asAeFluid(c), c.amount.asDisplay)
    }
  }

  private def asAeFluid(fluid: FluidAmount): AEFluidKey = {
    AEFluidKey.of(fluid.content, fluid.nbt.orNull)
  }

  private def fromAeFluid(fluidKey: AEFluidKey, amount: Long): FluidAmount = {
    FluidAmountUtil.from(fluidKey.getFluid, GenericUnit.fromForge(amount), Option(fluidKey.copyTag()))
  }
}
