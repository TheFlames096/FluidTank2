package com.kotori316.fluidtank.fluids

import cats.data.Chain
import com.kotori316.fluidtank.contents.{ChainTanksHandler, Tank}
import com.kotori316.fluidtank.tank.TileTank
import net.minecraft.world.level.material.Fluid

class FluidTanksHandler(s: Seq[TileTank]) extends ChainTanksHandler[FluidLike](true) {
  this.tanks = Chain.fromSeq(s.map(_.getTank))

  override def updateTanks(newTanks: Chain[Tank[FluidLike]]): Unit = {
    super.updateTanks(newTanks)
    s.zip(newTanks.toList).foreach {
      case (tile, value) => tile.setTank(value)
    }
  }

  def getTank: Chain[Tank[FluidLike]] = this.tanks
}
