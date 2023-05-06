package com.kotori316.fluidtank.forge.tank

import com.kotori316.fluidtank.tank.BlockVoidTank

class BlockVoidTankForge extends BlockVoidTank {
  override protected def createTankItem() = new ItemBlockVoidTankForge(this)
}
