package com.kotori316.fluidtank

import cats.{Hash, Show}
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.material.Fluid

object MCImplicits {
  implicit final val showPos: Show[BlockPos] = pos => s"(${pos.getX}, ${pos.getY}, ${pos.getZ})"
  implicit final val hashCompoundTag: Hash[CompoundTag] = Hash.fromUniversalHashCode
  implicit final val hashFluid: Hash[Fluid] = Hash.fromUniversalHashCode
}
