package com.kotori316.fluidtank.fabric.tank

import com.kotori316.fluidtank.contents.*
import com.kotori316.fluidtank.fabric.fluid.FabricConverter
import com.kotori316.fluidtank.fluids.{FluidAmount, FluidAmountUtil, FluidLike, fluidAccess}
import com.kotori316.fluidtank.tank.{ItemBlockTank, Tier, TileTank}
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.nbt.{CompoundTag, Tag}
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.material.Fluids

//noinspection UnstableApiUsage
class FabricTankItemStorage(private val context: ContainerItemContext) extends SingleSlotStorage[FluidVariant] {
  override def insert(resource: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long = {
    val tank = getTank
    move(tank, tank.fillOp, FabricConverter.fromVariant(resource, maxAmount), transaction)
  }

  override def extract(resource: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long = {
    val tank = getTank
    move(tank, tank.drainOp, FabricConverter.fromVariant(resource, maxAmount), transaction)
  }

  private def move(tank: Tank[FluidLike], op: Operations.TankOperation[FluidLike], fluid: FluidAmount, transaction: TransactionContext): Long = {
    val (_, rest, result) = op.run(DefaultTransferEnv, fluid)
    if (tank != result && this.context.exchange(createNewItem(result), 1, transaction) == 1) {
      val inserted = fluid - rest
      inserted.amount.asFabric
    } else {
      // Not inserted
      0
    }
  }

  override def isResourceBlank: Boolean = getTank.content.isContentEmpty

  override def getResource: FluidVariant = FabricConverter.toVariant(getTank.content, Fluids.EMPTY)

  override def getAmount: Long = getTank.content.amount.asFabric

  override def getCapacity: Long = getTank.capacity.asFabric

  def getTank: Tank[FluidLike] = {
    val tag = context.getItemVariant.getNbt
    if (tag == null || !tag.contains(BlockItem.BLOCK_ENTITY_TAG, Tag.TAG_COMPOUND)) {
      Tank(FluidAmountUtil.EMPTY, GenericUnit(getTier.getCapacity))
    } else {
      TankUtil.load(tag.getCompound(BlockItem.BLOCK_ENTITY_TAG).getCompound(TileTank.KEY_TANK))
    }
  }

  def getTier: Tier = context.getItemVariant.getItem.asInstanceOf[ItemBlockTank].blockTank.tier

  def createNewItem(newTank: Tank[FluidLike]): ItemVariant = {
    val itemTag = this.context.getItemVariant.copyOrCreateNbt()
    val tileTag = if (itemTag.contains(BlockItem.BLOCK_ENTITY_TAG, Tag.TAG_COMPOUND)) itemTag.getCompound(BlockItem.BLOCK_ENTITY_TAG) else new CompoundTag()
    if (newTank.isEmpty) {
      tileTag.remove(TileTank.KEY_TIER)
      tileTag.remove(TileTank.KEY_TANK)
    } else {
      tileTag.putString(TileTank.KEY_TIER, getTier.name())
      tileTag.put(TileTank.KEY_TANK, TankUtil.save(newTank))
    }
    if (tileTag.isEmpty) {
      itemTag.remove(BlockItem.BLOCK_ENTITY_TAG)
    } else {
      itemTag.put(BlockItem.BLOCK_ENTITY_TAG, tileTag)
    }
    ItemVariant.of(context.getItemVariant.getItem, if (itemTag.isEmpty) null else itemTag)
  }
}
