package com.kotori316.fluidtank.tank

import java.util

import net.minecraft.network.chat.Component
import net.minecraft.world.item.{BlockItem, Item, ItemStack, Rarity, TooltipFlag}
import net.minecraft.world.level.Level

class ItemBlockTank(val blockTank: BlockTank) extends BlockItem(blockTank, new Item.Properties()) {
  override def toString: String = blockTank.tier.getBlockName

  override def getRarity(stack: ItemStack): Rarity =
    if (BlockItem.getBlockEntityData(stack) != null) Rarity.RARE
    else Rarity.COMMON

  override def appendHoverText(stack: ItemStack, level: Level, tooltipComponents: util.List[Component], isAdvanced: TooltipFlag): Unit = {
    super.appendHoverText(stack, level, tooltipComponents, isAdvanced)
  }
}
