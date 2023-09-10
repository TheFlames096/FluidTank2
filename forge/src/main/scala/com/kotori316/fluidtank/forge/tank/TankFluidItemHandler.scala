package com.kotori316.fluidtank.forge.tank

import com.kotori316.fluidtank.contents.{GenericUnit, Tank, TankUtil}
import com.kotori316.fluidtank.fluids.{FluidAmount, FluidAmountUtil, FluidLike, fluidAccess}
import com.kotori316.fluidtank.forge.fluid.ForgeConverter.*
import com.kotori316.fluidtank.forge.fluid.TankFluidHandler
import com.kotori316.fluidtank.tank.{Tier, TileTank}
import net.minecraft.core.Direction
import net.minecraft.world.item.{BlockItem, ItemStack}
import net.minecraftforge.common.capabilities.{Capability, ForgeCapabilities, ICapabilityProvider}
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fluids.capability.{IFluidHandler, IFluidHandlerItem}
import org.jetbrains.annotations.VisibleForTesting

class TankFluidItemHandler(tier: Tier, stack: ItemStack) extends TankFluidHandler with ICapabilityProvider {

  private val handler = LazyOptional.of[IFluidHandlerItem](() => this)

  override def getCapability[T](capability: Capability[T], arg: Direction): LazyOptional[T] = {
    ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(capability, this.handler)
  }

  override def getContainer: ItemStack = stack

  override def getTank: Tank[FluidLike] = {
    val tag = BlockItem.getBlockEntityData(getContainer)
    if (tag == null || !tag.contains(TileTank.KEY_TANK)) return Tank(FluidAmountUtil.EMPTY, GenericUnit(tier.getCapacity))

    TankUtil.load(tag.getCompound(TileTank.KEY_TANK))
  }

  override def saveTank(tank: Tank[FluidLike]): Unit = {
    if (tank.isEmpty) {
      // remove tags
      val tag = getContainer.getOrCreateTagElement(BlockItem.BLOCK_ENTITY_TAG)
      tag.remove(TileTank.KEY_TANK)
      tag.remove(TileTank.KEY_TIER)
      if (tag.isEmpty) {
        getContainer.removeTagKey(BlockItem.BLOCK_ENTITY_TAG)
      }
    } else {
      val tankTag = TankUtil.save(tank)
      val tag = getContainer.getOrCreateTagElement(BlockItem.BLOCK_ENTITY_TAG)
      tag.put(TileTank.KEY_TANK, tankTag)
      tag.putString(TileTank.KEY_TIER, tier.name())
      // No need to save because the instance is shared.
    }
  }

  @VisibleForTesting
  def fill(fill: FluidAmount, execute: Boolean): Unit = {
    this.fill(fill.toStack, if (execute) IFluidHandler.FluidAction.EXECUTE else IFluidHandler.FluidAction.SIMULATE)
  }
}
