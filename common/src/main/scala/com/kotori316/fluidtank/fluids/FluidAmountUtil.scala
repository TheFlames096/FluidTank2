package com.kotori316.fluidtank.fluids

import com.kotori316.fluidtank.contents.{GenericAccess, GenericAmount, GenericUnit}
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.{BucketItem, ItemStack, Items}
import net.minecraft.world.level.material.{Fluid, Fluids}

object FluidAmountUtil {

  final val EMPTY: FluidAmount = GenericAmount(Fluids.EMPTY, GenericUnit.ZERO, Option.empty)
  final val BUCKET_WATER: FluidAmount = GenericAmount(Fluids.WATER, GenericUnit.ONE_BUCKET, Option.empty)
  final val BUCKET_LAVA: FluidAmount = GenericAmount(Fluids.LAVA, GenericUnit.ONE_BUCKET, Option.empty)

  def fromItem(stack: ItemStack): FluidAmount = {
    stack.getItem match {
      case Items.WATER_BUCKET => BUCKET_WATER
      case Items.LAVA_BUCKET => BUCKET_LAVA
      case bucket: BucketItem => GenericAmount(PlatformFluidAccess.getInstance().getBucketContent(bucket), GenericUnit.ONE_BUCKET, Option.empty)
      case _ => PlatformFluidAccess.getInstance().getFluidContained(stack)
        .map(f => GenericAmount(f, GenericUnit.ZERO, Option.empty))
        .getOrElse(EMPTY)
    }
  }

  def fromTag(tag: CompoundTag): FluidAmount = implicitly[GenericAccess[Fluid]].read(tag)
}
