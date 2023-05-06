package com.kotori316.fluidtank.forge.tank

import java.util.function.Consumer

import com.kotori316.fluidtank.forge.render.RenderItemTank
import com.kotori316.fluidtank.tank.ItemBlockTank
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
import net.minecraftforge.client.extensions.common.IClientItemExtensions

class ItemBlockTankForge(b: BlockTankForge) extends ItemBlockTank(b) {

  override def initializeClient(consumer: Consumer[IClientItemExtensions]): Unit = {
    consumer.accept(new IClientItemExtensions {
      override def getCustomRenderer: BlockEntityWithoutLevelRenderer = RenderItemTank.INSTANCE
    })
  }

}
