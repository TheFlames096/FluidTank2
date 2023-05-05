package com.kotori316.fluidtank.tank

import net.minecraft.world.item.{BlockItem, Item, ItemStack, Rarity}

class ItemBlockTank(val blockTank: BlockTank) extends BlockItem(blockTank, new Item.Properties()) {
  final val registryName = blockTank.registryName

  override def toString: String = registryName.toString

  override def getRarity(stack: ItemStack): Rarity =
    if (BlockItem.getBlockEntityData(stack) != null) Rarity.RARE
    else Rarity.COMMON
}
