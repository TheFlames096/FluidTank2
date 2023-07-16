package com.kotori316.fluidtank.integration.tooltip

import com.kotori316.fluidtank.FluidTankCommon
import com.kotori316.fluidtank.fluids.{FluidAmountUtil, PlatformFluidAccess}
import com.kotori316.fluidtank.tank.{TileTank, TileVoidTank}
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntity

import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale
import scala.util.chaining.scalaUtilChainingOps

object TooltipContent {
  final val JADE_TOOLTIP_UID = new ResourceLocation(FluidTankCommon.modId, "jade_plugin")
  private final val KEY_TIER = TileTank.KEY_TIER
  private final val KEY_FLUID = "fluid"
  private final val KEY_CAPACITY = "capacity"
  private final val KEY_COMPARATOR = "comparator"
  private final val KEY_CREATIVE = "hasCreative"

  final def addServerData(compoundTag: CompoundTag, entity: BlockEntity): Unit = {
    entity match {
      case voidTank: TileVoidTank =>
        compoundTag.putString(KEY_TIER, voidTank.tier.toString)
      case tank: TileTank =>
        compoundTag.putString(KEY_TIER, tank.tier.toString)
        compoundTag.put(KEY_FLUID, tank.getConnection.getContent.getOrElse(FluidAmountUtil.EMPTY).getTag)
        compoundTag.putBoolean(KEY_CREATIVE, tank.getConnection.hasCreative)
        if (!tank.getConnection.hasCreative) {
          compoundTag.putLong(KEY_CAPACITY, tank.getConnection.capacity.asDisplay)
          compoundTag.putInt(KEY_COMPARATOR, tank.getConnection.getComparatorLevel)
        }
    }
  }

  final def getTooltipText(tankData: CompoundTag, tank: TileTank, isShort: Boolean, isCompact: Boolean, locale: Locale): Seq[Component] = {
    val numberFormat: Long => String = if (isCompact) {
      val formatter = NumberFormat.getCompactNumberInstance(locale, NumberFormat.Style.SHORT)
        .tap(_.setMinimumFractionDigits(1))
        .tap(_.setRoundingMode(RoundingMode.DOWN))
      (n: Long) => formatter.format(n)
    } else {
      (n: Long) => n.toString
    }
    val fluid = FluidAmountUtil.fromTag(tankData.getCompound(KEY_FLUID))
    val fluidName = if (fluid.nonEmpty) {
      PlatformFluidAccess.getInstance().getDisplayName(fluid)
    } else {
      Component.translatable("chat.fluidtank.empty")
    }

    if (isShort) {
      if (tank.isInstanceOf[TileVoidTank]) Seq.empty
      else if (tankData.getBoolean(KEY_CREATIVE)) {
        Seq(fluidName)
      } else {
        Seq(Component.translatable("fluidtank.waila.short",
          fluidName,
          numberFormat(fluid.amount.asDisplay),
          numberFormat(tankData.getLong(KEY_CAPACITY)),
        ))
      }
    } else {
      val tier = Seq(Component.translatable("fluidtank.waila.tier", tank.tier.toString))
      if (tank.isInstanceOf[TileVoidTank]) {
        tier
      }
      else if (tankData.getBoolean(KEY_CREATIVE)) {
        tier ++ Seq(
          Component.translatable("fluidtank.waila.content", fluidName),
        )
      } else {
        tier ++ Seq(
          Component.translatable("fluidtank.waila.content", fluidName),
          Component.translatable("fluidtank.waila.amount", numberFormat(fluid.amount.asDisplay)),
          Component.translatable("fluidtank.waila.capacity", tankData.getLong(KEY_CAPACITY)),
          Component.translatable("fluidtank.waila.comparator", tankData.getLong(KEY_COMPARATOR)),
        )
      }
    }
  }
}
