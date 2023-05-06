package com.kotori316.fluidtank.forge.tank

import com.kotori316.fluidtank.tank.{BlockTank, Tier}

class BlockTankForge(t: Tier) extends BlockTank(t) {
  override protected def createTankItem() = new ItemBlockTankForge(this)
}
