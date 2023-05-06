package com.kotori316.fluidtank.forge.tank

import com.kotori316.fluidtank.tank.BlockCreativeTank

class BlockCreativeTankForge extends BlockCreativeTank {
  override protected def createTankItem() = new ItemBlockCreativeTankForge(this)
}
