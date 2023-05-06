package com.kotori316.fluidtank.tank

import java.util

import net.minecraft.network.chat.Component
import net.minecraft.world.item.{ItemStack, TooltipFlag}
import net.minecraft.world.level.Level

class ItemBlockVoidTank(b: BlockTank) extends ItemBlockTank(b) {
  override def appendHoverText(stack: ItemStack, level: Level, tooltip: util.List[Component], isAdvanced: TooltipFlag): Unit = {
  }
}
