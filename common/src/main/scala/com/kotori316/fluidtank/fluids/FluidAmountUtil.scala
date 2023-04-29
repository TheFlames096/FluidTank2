package com.kotori316.fluidtank.fluids

import com.kotori316.fluidtank.contents.{GenericAccess, GenericAmount, GenericUnit}
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.{ItemStack, Items}
import net.minecraft.world.level.material.{Fluid, Fluids}

object FluidAmountUtil {

  final val EMPTY: FluidAmount = from(Fluids.EMPTY, GenericUnit.ZERO, Option.empty)
  final val BUCKET_WATER: FluidAmount = from(Fluids.WATER, GenericUnit.ONE_BUCKET, Option.empty)
  final val BUCKET_LAVA: FluidAmount = from(Fluids.LAVA, GenericUnit.ONE_BUCKET, Option.empty)

  def from(fluid: Fluid, genericUnit: GenericUnit, nbt: Option[CompoundTag]): FluidAmount = {
    GenericAmount(fluid, genericUnit, nbt)
  }

  def from(fluid: Fluid, genericUnit: GenericUnit): FluidAmount = from(fluid, genericUnit, Option.empty)

  def fromItem(stack: ItemStack): FluidAmount = {
    stack.getItem match {
      case Items.WATER_BUCKET => BUCKET_WATER
      case Items.LAVA_BUCKET => BUCKET_LAVA
      case Items.BUCKET => EMPTY
      case _ => PlatformFluidAccess.getInstance().getFluidContained(stack)
    }
  }

  def fromTag(tag: CompoundTag): FluidAmount = implicitly[GenericAccess[Fluid]].read(tag)
}
