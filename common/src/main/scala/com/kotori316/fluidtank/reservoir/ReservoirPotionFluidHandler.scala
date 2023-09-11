package com.kotori316.fluidtank.reservoir

import com.kotori316.fluidtank.contents.Operations.TankOperation
import com.kotori316.fluidtank.contents.{DefaultTransferEnv, GenericAmount, Tank}
import com.kotori316.fluidtank.fluids.{FluidAmount, FluidAmountUtil, FluidLike, PlatformFluidAccess, VanillaPotion}
import com.kotori316.fluidtank.potions.PotionFluidHandler
import net.minecraft.world.item.ItemStack

final class ReservoirPotionFluidHandler(itemReservoir: ItemReservoir, stack: ItemStack) extends PotionFluidHandler {
  private def getTank: Tank[FluidLike] = itemReservoir.getTank(stack)

  override def fill(toFill: FluidAmount, vanillaPotion: VanillaPotion): PlatformFluidAccess.TransferStack = {
    move(toFill, getTank.fillOp)
  }

  override def drain(toDrain: FluidAmount, vanillaPotion: VanillaPotion): PlatformFluidAccess.TransferStack = {
    move(toDrain, getTank.drainOp)
  }

  private def move(source: FluidAmount, op: TankOperation[FluidLike]): PlatformFluidAccess.TransferStack = {
    val (_, rest, newTank) = op.run(DefaultTransferEnv, source)

    val moved = source - rest
    if (moved.isEmpty) {
      new PlatformFluidAccess.TransferStack(FluidAmountUtil.EMPTY, stack, false)
    } else {
      val copy = stack.copy()
      itemReservoir.saveTank(copy, newTank)
      new PlatformFluidAccess.TransferStack(moved, copy)
    }
  }

  override def getContent: GenericAmount[FluidLike] = getTank.content

  override def isValidHandler: Boolean = getContent.content.isInstanceOf[VanillaPotion]
}
