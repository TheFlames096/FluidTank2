package com.kotori316.fluidtank.tank

import com.kotori316.fluidtank.contents.VoidTank
import com.kotori316.fluidtank.fluids.fluidAccess
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

class TileVoidTank(p: BlockPos, s: BlockState)
  extends TileTank(Tier.CREATIVE, PlatformTankAccess.getInstance().getVoidType, p, s) {

  setTank(VoidTank.apply)
}
