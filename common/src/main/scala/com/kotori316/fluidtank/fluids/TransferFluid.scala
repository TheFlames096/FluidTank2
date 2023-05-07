package com.kotori316.fluidtank.fluids

import net.minecraft.core.BlockPos
import net.minecraft.sounds.{SoundEvent, SoundSource}
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

object TransferFluid {
  case class Result(stack: ItemStack, sound: Option[SoundEvent])

  def transferFluid(connection: FluidConnection, playerStack: ItemStack): Option[Result] = {
    tryFillTank(connection, playerStack) orElse tryFillContainer(connection, playerStack)
  }

  private def tryFillTank(connection: FluidConnection, playerStack: ItemStack): Option[Result] = {
    val toFill = PlatformFluidAccess.getInstance().getFluidContained(playerStack)
    if (toFill.isEmpty) return None

    val fillSimulate = connection.getHandler.fill(toFill, execute = false)
    if (fillSimulate.isEmpty) return None

    val canDrainFromItem = PlatformFluidAccess.getInstance().drainItem(fillSimulate, playerStack)
    if (canDrainFromItem.getLeft.isEmpty) return None

    // Assuming canDrainFromItem.getLeft.amount <= fillSimulate.amount
    // Also, tank can accept any fluid whose amount <= fillSimulate.amount
    val filled: FluidAmount = connection.getHandler.fill(canDrainFromItem.getLeft, execute = true)
    val drainedItem: ItemStack = PlatformFluidAccess.getInstance().drainItem(filled, playerStack).getRight
    Option(Result(drainedItem, Option(PlatformFluidAccess.getInstance().getEmptySound(filled))))
  }

  private def tryFillContainer(connection: FluidConnection, playerStack: ItemStack): Option[Result] = {
    val toFillOption = connection.getContent
    if (toFillOption.isEmpty) return None

    val toFill = toFillOption.get
    val bucketContent = PlatformFluidAccess.getInstance().getFluidContained(playerStack)
    if (bucketContent.nonEmpty && !toFill.contentEqual(bucketContent)) return None

    val fillSimulate = PlatformFluidAccess.getInstance().fillItem(toFill, playerStack).getLeft
    if (fillSimulate.isEmpty) return None

    val filled = PlatformFluidAccess.getInstance().fillItem(fillSimulate, playerStack)
    connection.getHandler.drain(filled.getLeft, execute = true)
    Option(Result(filled.getRight, Option(PlatformFluidAccess.getInstance().getFillSound(filled.getLeft))))
  }

  def setItem(player: Player, hand: InteractionHand, result: Result, blockPos: BlockPos): Unit = {
    if (!player.isCreative) {
      // set item
      if (player.getItemInHand(hand).getCount == 1) {
        // replace
        player.setItemInHand(hand, result.stack)
      } else {
        // give
        player.getItemInHand(hand).shrink(1)
        if (!player.addItem(result.stack)) {
          player.drop(result.stack, false)
        }
      }
    }
    // Sound, the player in parameter means "except", so passing null
    result.sound.foreach(s => player.level.playSound(null, blockPos, s, SoundSource.BLOCKS, 1f, 1f))
  }
}
