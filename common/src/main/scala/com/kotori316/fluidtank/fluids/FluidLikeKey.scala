package com.kotori316.fluidtank.fluids

import cats.{Hash, Show}
import com.kotori316.fluidtank.contents.GenericUnit
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag

case class FluidLikeKey(fluid: FluidLike, tag: Option[CompoundTag]) {
  def isEmpty: Boolean = fluid == FluidLike.FLUID_EMPTY

  def isDefined: Boolean = !isEmpty

  def toAmount(genericUnit: GenericUnit): FluidAmount =
    FluidAmountUtil.from(this.fluid, genericUnit, this.tag)
}

object FluidLikeKey {
  def apply(fluid: FluidLike, tag: Option[CompoundTag]): FluidLikeKey = new FluidLikeKey(fluid, tag.map(_.copy()))

  def from(fluidAmount: FluidAmount): FluidLikeKey = FluidLikeKey(fluidAmount.content, fluidAmount.nbt)

  implicit val FluidKeyHash: Hash[FluidLikeKey] = Hash.fromUniversalHashCode
  implicit val FluidKeyShow: Show[FluidLikeKey] = key =>
    key.fluid match {
      case VanillaFluid(fluid) =>
        key.tag match {
          case Some(tag) => f"FluidLikeKey(${BuiltInRegistries.FLUID.getKey(fluid)}, $tag)"
          case None => f"FluidLikeKey(${BuiltInRegistries.FLUID.getKey(fluid)})"
        }
      case VanillaPotion(potionType) =>
        f"FluidLikeKey($potionType, ${key.tag.orNull})"
    }
}
