package com.kotori316.fluidtank.fluids

import cats.Hash
import net.minecraft.world.level.material.{Fluid, Fluids}

sealed trait FluidLike {

}

case class VanillaFluid(fluid: Fluid) extends FluidLike

case class VanillaPotion(potionType: PotionType) extends FluidLike

object FluidLike {
  implicit final val hashFluidLike: Hash[FluidLike] = Hash.fromUniversalHashCode

  final val FLUID_EMPTY = VanillaFluid(Fluids.EMPTY)
  final val FLUID_WATER = VanillaFluid(Fluids.WATER)
  final val FLUID_LAVA = VanillaFluid(Fluids.LAVA)

  final val POTION_NORMAL = VanillaPotion(PotionType.NORMAL)
  final val POTION_SPLASH = VanillaPotion(PotionType.SPLASH)
  final val POTION_LINGERING = VanillaPotion(PotionType.LINGERING)
}
