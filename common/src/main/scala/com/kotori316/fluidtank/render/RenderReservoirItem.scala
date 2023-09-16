package com.kotori316.fluidtank.render

import com.kotori316.fluidtank.FluidTankCommon
import com.kotori316.fluidtank.reservoir.ItemReservoir
import com.kotori316.fluidtank.tank.Tier
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.client.renderer.{BlockEntityWithoutLevelRenderer, MultiBufferSource}
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.world.item.{ItemDisplayContext, ItemStack}

import java.util.Locale

class RenderReservoirItem extends BlockEntityWithoutLevelRenderer(Minecraft.getInstance.getBlockEntityRenderDispatcher, Minecraft.getInstance.getEntityModels) {
  private var model: ReservoirModel = _

  override def renderByItem(stack: ItemStack, displayContext: ItemDisplayContext, poseStack: PoseStack, buffer: MultiBufferSource, packedLight: Int, packedOverlay: Int): Unit = {
    poseStack.pushPose()
    poseStack.scale(1.0F, 1.0F, 1.0F)
    poseStack.translate(0, 0, 0.5f)
    val vertexConsumer = ItemRenderer.getFoilBufferDirect(buffer,
      this.model.renderType(RenderReservoirItem.textureNameMap(stack.getItem.asInstanceOf[ItemReservoir].tier)),
      true, stack.hasFoil)
    RenderSystem.enableCull()
    this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f)
    poseStack.popPose()
  }

  override def onResourceManagerReload(resourceManager: ResourceManager): Unit = {
    this.model = new ReservoirModel(Minecraft.getInstance.getEntityModels.bakeLayer(ReservoirModel.LOCATION))
  }
}

object RenderReservoirItem {
  /**
   * Just for Forge.
   * Use platform-specific class in Fabric
   */
  val INSTANCE = new RenderReservoirItem

  private final val textureNameMap: Map[Tier, ResourceLocation] = Tier.values().map(t =>
      (t, new ResourceLocation(FluidTankCommon.modId, s"textures/item/reservoir_${t.name().toLowerCase(Locale.ROOT)}.png")))
    .toMap
}
