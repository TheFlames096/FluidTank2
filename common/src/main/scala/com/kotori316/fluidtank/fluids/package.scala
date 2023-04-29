package com.kotori316.fluidtank

import cats.Hash
import com.kotori316.fluidtank.contents.{GenericAccess, GenericAmount, GenericUnit}
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.{CompoundTag, Tag => NbtTag}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.material.{Fluid, Fluids}

import scala.reflect.ClassTag

package object fluids {

  type FluidAmount = GenericAmount[Fluid]
  implicit final val fluidHash: Hash[Fluid] = Hash.fromUniversalHashCode
  implicit final val fluidAccess: GenericAccess[Fluid] = new FluidAccess
  private final val KEY_FLUID = "fluid"
  private final val KEY_FORGE_AMOUNT = "amount"
  private final val KEY_FABRIC_AMOUNT = "fabric_amount"
  private final val KEY_AMOUNT_GENERIC = "amount_generic"
  private final val KEY_TAG = "tag"

  private class FluidAccess extends GenericAccess[Fluid] {
    override def isEmpty(a: Fluid): Boolean = a == empty

    override def isGaseous(a: Fluid): Boolean = PlatformFluidAccess.getInstance().isGaseous(a)

    override def getKey(a: Fluid): ResourceLocation = BuiltInRegistries.FLUID.getKey(a)

    override def empty: Fluid = Fluids.EMPTY

    override def write(amount: GenericAmount[Fluid]): CompoundTag = {
      val tag = new CompoundTag()

      tag.putString(KEY_FLUID, getKey(amount.content).toString)
      tag.putByteArray(KEY_AMOUNT_GENERIC, amount.amount.asByteArray)
      amount.nbt.foreach(t => tag.put(KEY_TAG, t))

      tag
    }

    override def read(tag: CompoundTag): GenericAmount[Fluid] = {
      val key: Fluid = BuiltInRegistries.FLUID.get(new ResourceLocation(tag.getString(KEY_FLUID)))
      val amount: GenericUnit = {
        if (tag.contains(KEY_AMOUNT_GENERIC, NbtTag.TAG_BYTE_ARRAY)) GenericUnit.fromByteArray(tag.getByteArray(KEY_AMOUNT_GENERIC))
        else if (tag.contains(KEY_FABRIC_AMOUNT)) GenericUnit.fromFabric(tag.getLong(KEY_FABRIC_AMOUNT))
        else GenericUnit.fromForge(tag.getInt(KEY_FORGE_AMOUNT))
      }
      val fluidTag: Option[CompoundTag] = Option.when(tag.contains(KEY_TAG))(tag.getCompound(KEY_TAG))

      new GenericAmount[Fluid](key, amount, fluidTag)
    }

    override def classTag: ClassTag[Fluid] = implicitly[ClassTag[Fluid]]
  }
}
