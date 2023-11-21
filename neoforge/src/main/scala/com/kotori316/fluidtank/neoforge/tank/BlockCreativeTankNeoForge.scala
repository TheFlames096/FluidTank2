package com.kotori316.fluidtank.neoforge.tank

import com.kotori316.fluidtank.tank.BlockCreativeTank
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class BlockCreativeTankNeoForge extends BlockCreativeTank {
  override protected def createTankItem() = new ItemBlockCreativeTankNeoForge(this)

  override def newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = new TileCreativeTankNeoForge(pos, state)
}
