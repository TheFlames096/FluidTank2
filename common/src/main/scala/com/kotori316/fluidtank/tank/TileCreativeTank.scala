package com.kotori316.fluidtank.tank

import com.kotori316.fluidtank.contents.CreativeTank
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

class TileCreativeTank(p: BlockPos, s: BlockState)
  extends TileTank(Tier.CREATIVE, PlatformTileAccess.getInstance().getCreativeType, p, s) {

  setTank(new CreativeTank(this.getTank.content, this.getTank.capacity))
}
