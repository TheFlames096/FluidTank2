package com.kotori316.fluidtank.fluids

import cats.{Hash, Show}
import com.kotori316.fluidtank.contents.GenericUnit
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.material.{Fluid, Fluids}

case class FluidKey(fluid: Fluid, tag: Option[CompoundTag]) {
  def isEmpty: Boolean = fluid == Fluids.EMPTY

  def isDefined: Boolean = !isEmpty

  def toAmount(genericUnit: GenericUnit): FluidAmount =
    FluidAmountUtil.from(this.fluid, genericUnit, this.tag)
}

object FluidKey {
  def apply(fluid: Fluid, tag: Option[CompoundTag]): FluidKey = new FluidKey(fluid, tag.map(_.copy()))

  def from(fluidAmount: FluidAmount): FluidKey = FluidKey(fluidAmount.content, fluidAmount.nbt)

  implicit val FluidKeyHash: Hash[FluidKey] = Hash.fromUniversalHashCode
  implicit val FluidKeyShow: Show[FluidKey] = key =>
    key.tag match {
      case Some(tag) => f"FluidKey(${BuiltInRegistries.FLUID.getKey(key.fluid)}, $tag)"
      case None => f"FluidKey(${BuiltInRegistries.FLUID.getKey(key.fluid)})"
    }
}
