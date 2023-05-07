package com.kotori316.fluidtank.forge.tank

import com.kotori316.fluidtank.tank.BlockCreativeTank
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class BlockCreativeTankForge extends BlockCreativeTank {
  override protected def createTankItem() = new ItemBlockCreativeTankForge(this)

  override def newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = new TileCreativeTankForge(pos, state)
}
