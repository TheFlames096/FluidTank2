package com.kotori316.fluidtank.fluids

import cats.Hash
import com.kotori316.fluidtank.FluidTankCommon
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.alchemy.PotionUtils
import net.minecraft.world.level.material.{Fluid, Fluids}

import java.util.Locale

sealed trait FluidLike {
  def isGaseous: Boolean

  def getKey: ResourceLocation
}

case class VanillaFluid(fluid: Fluid) extends FluidLike {
  override def isGaseous: Boolean = PlatformFluidAccess.getInstance().isGaseous(fluid)

  override def getKey: ResourceLocation = BuiltInRegistries.FLUID.getKey(fluid)
}

case class VanillaPotion(potionType: PotionType) extends FluidLike {
  override def isGaseous: Boolean = false

  override def getKey: ResourceLocation = {
    new ResourceLocation(FluidTankCommon.modId, ("potion_" + potionType.name()).toLowerCase(Locale.ROOT))
  }

  def getVanillaPotionName(nbt: Option[CompoundTag]): Component = {
    val potion = PotionUtils.getPotion(nbt.orNull)
    val prefix = potionType.getItem.getDescriptionId + ".effect."
    Component.translatable(potion.getName(prefix))
  }
}

object FluidLike {
  implicit final val hashFluidLike: Hash[FluidLike] = Hash.fromUniversalHashCode

  final val FLUID_EMPTY = VanillaFluid(Fluids.EMPTY)
  final val FLUID_WATER = VanillaFluid(Fluids.WATER)
  final val FLUID_LAVA = VanillaFluid(Fluids.LAVA)

  final val POTION_NORMAL = VanillaPotion(PotionType.NORMAL)
  final val POTION_SPLASH = VanillaPotion(PotionType.SPLASH)
  final val POTION_LINGERING = VanillaPotion(PotionType.LINGERING)

  private final val FLUID_CACHE: scala.collection.mutable.Map[Fluid, VanillaFluid] = scala.collection.mutable.Map(
    Fluids.EMPTY -> FLUID_EMPTY,
    Fluids.WATER -> FLUID_WATER,
    Fluids.LAVA -> FLUID_LAVA,
  )
  private final val POTION_CACHE: scala.collection.mutable.Map[PotionType, VanillaPotion] = scala.collection.mutable.Map(
    PotionType.NORMAL -> POTION_NORMAL,
    PotionType.SPLASH -> POTION_SPLASH,
    PotionType.LINGERING -> POTION_LINGERING,
  )

  def of(fluid: Fluid): VanillaFluid = {
    FLUID_CACHE.getOrElseUpdate(fluid, VanillaFluid(fluid))
  }

  def of(potionType: PotionType): VanillaPotion = {
    POTION_CACHE.getOrElseUpdate(potionType, VanillaPotion(potionType))
  }

  def asFluid(fluidLike: FluidLike, fallback: Fluid): Fluid = {
    fluidLike match {
      case VanillaFluid(fluid) => fluid
      case VanillaPotion(_) => fallback
    }
  }

  def fromResourceLocation(key: ResourceLocation): FluidLike = {
    if (key.getNamespace == FluidTankCommon.modId && key.getPath.startsWith("potion_")) {
      val potionType = PotionType.valueOf(key.getPath.substring(7).toUpperCase(Locale.ROOT))
      FluidLike.of(potionType)
    } else {
      // this is a fluid
      FluidLike.of(BuiltInRegistries.FLUID.get(key))
    }
  }
}
