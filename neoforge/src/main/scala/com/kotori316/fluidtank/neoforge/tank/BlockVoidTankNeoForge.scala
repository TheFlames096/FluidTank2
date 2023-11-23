package com.kotori316.fluidtank.neoforge.tank

import com.kotori316.fluidtank.tank.BlockVoidTank
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class BlockVoidTankNeoForge extends BlockVoidTank {
  override protected def createTankItem() = new ItemBlockVoidTankNeoForge(this)

  override def newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = new TileVoidTankNeoForge(pos, state)
}
