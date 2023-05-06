package com.kotori316.fluidtank.forge.tank

import java.util.function.Consumer

import com.kotori316.fluidtank.forge.render.RenderItemTank
import com.kotori316.fluidtank.tank.ItemBlockCreativeTank
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
import net.minecraftforge.client.extensions.common.IClientItemExtensions

class ItemBlockCreativeTankForge(b: BlockCreativeTankForge) extends ItemBlockCreativeTank(b) {

  override def initializeClient(consumer: Consumer[IClientItemExtensions]): Unit = {
    consumer.accept(new IClientItemExtensions {
      override def getCustomRenderer: BlockEntityWithoutLevelRenderer = RenderItemTank.INSTANCE
    })
  }

}
