package com.kotori316.fluidtank.forge.tank

import com.kotori316.fluidtank.tank.BlockVoidTank
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class BlockVoidTankForge extends BlockVoidTank {
  override protected def createTankItem() = new ItemBlockVoidTankForge(this)

  override def newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = new TileVoidTankForge(pos, state)
}
