package com.kotori316.fluidtank.forge.tank

import com.kotori316.fluidtank.tank.{BlockTank, Tier}
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class BlockTankForge(t: Tier) extends BlockTank(t) {
  override protected def createTankItem() = new ItemBlockTankForge(this)

  override def newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = {
    new TileTankForge(t, pos, state)
  }
}
