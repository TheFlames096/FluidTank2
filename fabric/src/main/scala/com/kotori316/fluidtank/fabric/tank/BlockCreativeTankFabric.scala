package com.kotori316.fluidtank.fabric.tank

import com.kotori316.fluidtank.tank.BlockCreativeTank
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class BlockCreativeTankFabric extends BlockCreativeTank {
  override def newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = new TileCreativeTankFabric(pos, state)
}
