package com.kotori316.fluidtank.forge.tank

import com.kotori316.fluidtank.contents.{DefaultTransferEnv, GenericUnit, Tank, TankUtil}
import com.kotori316.fluidtank.fluids.{FluidAmount, FluidAmountUtil, fluidAccess}
import com.kotori316.fluidtank.forge.fluid.ForgeConverter._
import com.kotori316.fluidtank.tank.{Tier, TileTank}
import net.minecraft.core.Direction
import net.minecraft.world.item.{BlockItem, ItemStack}
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.common.capabilities.{Capability, ForgeCapabilities, ICapabilityProvider}
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.{IFluidHandler, IFluidHandlerItem}
import org.jetbrains.annotations.VisibleForTesting

class TankFluidItemHandler(tier: Tier, stack: ItemStack) extends IFluidHandlerItem with ICapabilityProvider {

  private val handler = LazyOptional.of[IFluidHandlerItem](() => this)

  override def getCapability[T](capability: Capability[T], arg: Direction): LazyOptional[T] = {
    ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(capability, this.handler)
  }

  override def getContainer: ItemStack = stack

  @VisibleForTesting
  private[tank] def getTank: Tank[Fluid] = {
    val tag = BlockItem.getBlockEntityData(getContainer)
    if (tag == null || !tag.contains(TileTank.KEY_TANK)) return Tank(FluidAmountUtil.EMPTY, GenericUnit(tier.getCapacity))

    TankUtil.load(tag.getCompound(TileTank.KEY_TANK))
  }

  @VisibleForTesting
  private[tank] def updateTank(tank: Tank[Fluid]): Unit = {
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

  override def getTanks: Int = 1

  override def getFluidInTank(i: Int): FluidStack = getTank.content.toStack

  override def getTankCapacity(i: Int): Int = getTank.capacity.asForge

  override def isFluidValid(i: Int, fluidStack: FluidStack): Boolean = true

  override def fill(fill: FluidStack, fluidAction: IFluidHandler.FluidAction): Int = {
    if (fill.isEmpty) return 0
    val tank = getTank
    val (_, rest, newTank) = tank.fillOp.run(DefaultTransferEnv, fill.toAmount)
    if (fluidAction.execute()) updateTank(newTank)
    fill.getAmount - rest.amount.asForge
  }

  override def drain(drain: FluidStack, fluidAction: IFluidHandler.FluidAction): FluidStack = {
    val tank = getTank
    if (tank.isEmpty) {
      FluidStack.EMPTY
    } else {
      drainInternal(tank, drain.toAmount, fluidAction)
    }
  }

  override def drain(amount: Int, fluidAction: IFluidHandler.FluidAction): FluidStack = {
    val tank = getTank
    if (tank.isEmpty) {
      FluidStack.EMPTY
    } else {
      drainInternal(tank, tank.content.setAmount(GenericUnit.fromForge(amount)), fluidAction)
    }
  }

  private def drainInternal(tank: Tank[Fluid], drainAmount: FluidAmount, fluidAction: IFluidHandler.FluidAction): FluidStack = {
    val (_, rest, newTank) = tank.drainOp.run(DefaultTransferEnv, drainAmount)
    if (fluidAction.execute()) updateTank(newTank)
    (drainAmount - rest).toStack
  }
}
