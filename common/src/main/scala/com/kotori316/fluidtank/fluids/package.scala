package com.kotori316.fluidtank

import cats.Hash
import com.kotori316.fluidtank.connection.ConnectionHelper
import com.kotori316.fluidtank.contents.{GenericAccess, GenericAmount, GenericUnit}
import com.kotori316.fluidtank.tank.TileTank
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.material.{Fluid, Fluids}

import scala.reflect.ClassTag

package object fluids {

  type FluidAmount = GenericAmount[Fluid]
  implicit final val fluidHash: Hash[Fluid] = Hash.fromUniversalHashCode
  implicit final val fluidAccess: GenericAccess[Fluid] = new FluidAccess
  implicit final val fluidConnectionHelper: ConnectionHelper.Aux[TileTank, Fluid, FluidTanksHandler] = FluidConnection.fluidConnectionHelper

  private class FluidAccess extends GenericAccess[Fluid] {
    override def isEmpty(a: Fluid): Boolean = a == empty

    override def isGaseous(a: Fluid): Boolean = PlatformFluidAccess.getInstance().isGaseous(a)

    override def getKey(a: Fluid): ResourceLocation = BuiltInRegistries.FLUID.getKey(a)

    override def fromKey(key: ResourceLocation): Fluid = BuiltInRegistries.FLUID.get(key)

    override def empty: Fluid = Fluids.EMPTY

    override def classTag: ClassTag[Fluid] = implicitly[ClassTag[Fluid]]

    override def newInstance(content: Fluid, amount: GenericUnit, nbt: Option[CompoundTag]): FluidAmount =
      GenericAmount(content, amount, nbt)
  }
}
