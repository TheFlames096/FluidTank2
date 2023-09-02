package com.kotori316.fluidtank.reservoir

import com.kotori316.fluidtank.contents.{GenericUnit, Tank, TankUtil}
import com.kotori316.fluidtank.fluids.{FluidAmountUtil, FluidLike, PlatformFluidAccess, VanillaFluid, VanillaPotion, fluidAccess}
import com.kotori316.fluidtank.tank.{Tier, TileTank}
import net.minecraft.network.chat.Component
import net.minecraft.world.item.{Item, ItemStack, Rarity, TooltipFlag, UseAnim}
import net.minecraft.world.level.Level

import java.util
import java.util.Locale

class ItemReservoir(val tier: Tier) extends Item(new Item.Properties().stacksTo(1)) {
  override def toString: String = s"ItemReservoir(${tier.name().toLowerCase(Locale.ROOT)})"

  override def getUseAnimation(stack: ItemStack): UseAnim = {
    getTank(stack).content.content match {
      case _: VanillaFluid => super.getUseAnimation(stack)
      case _: VanillaPotion => UseAnim.DRINK
    }
  }

  override def getUseDuration(stack: ItemStack): Int = {
    getTank(stack).content.content match {
      case _: VanillaFluid => super.getUseDuration(stack)
      case _: VanillaPotion => Item.EAT_DURATION
    }
  }

  override def getRarity(stack: ItemStack): Rarity = {
    if (stack.getTagElement(TileTank.KEY_TANK) != null) Rarity.UNCOMMON
    else super.getRarity(stack)
  }

  override def appendHoverText(stack: ItemStack, level: Level, tooltip: util.List[Component], isAdvanced: TooltipFlag): Unit = {
    super.appendHoverText(stack, level, tooltip, isAdvanced)
    val tank = getTank(stack)
    if (tank.isEmpty) {
      tooltip.add(Component.translatable("fluidtank.waila.capacity", GenericUnit.asForgeFromBigInt(tier.getCapacity)))
    } else {
      val fluid = tank.content
      val capacity = tank.capacity
      tooltip.add(Component.translatable("fluidtank.waila.short",
        PlatformFluidAccess.getInstance().getDisplayName(fluid), fluid.amount.asDisplay, capacity.asDisplay))
    }
  }

  def getTank(stack: ItemStack): Tank[FluidLike] = {
    val tag = stack.getTagElement(TileTank.KEY_TANK)
    if (tag == null) {
      Tank(FluidAmountUtil.EMPTY, GenericUnit(tier.getCapacity))
    } else {
      TankUtil.load(tag)
    }
  }

  def saveTank(stack: ItemStack, tank: Tank[FluidLike]): Unit = {
    if (tank.isEmpty) {
      stack.removeTagKey(TileTank.KEY_TANK)
    } else {
      stack.addTagElement(TileTank.KEY_TANK, TankUtil.save(tank))
    }
  }
}
