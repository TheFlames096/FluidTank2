package com.kotori316.fluidtank.fabric.fluid

import com.kotori316.fluidtank.contents.GenericUnit
import com.kotori316.fluidtank.fluids.{FluidAmount, FluidAmountUtil}
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant

//noinspection UnstableApiUsage
object FabricConverter {
  def toVariant(amount: FluidAmount): FluidVariant = {
    FluidVariant.of(amount.content, amount.nbt.orNull)
  }

  def fabricAmount(amount: FluidAmount): Long = amount.amount.asFabric

  def fromVariant(variant: FluidVariant, fabricAmount: Long): FluidAmount = {
    FluidAmountUtil.from(variant.getFluid, GenericUnit.fromFabric(fabricAmount), Option(variant.getNbt))
  }
}
