package com.kotori316.fluidtank.tank

import com.kotori316.fluidtank.contents.{GenericUnit, Tank, TankUtil}
import com.kotori316.fluidtank.fluids.FluidConnection._
import com.kotori316.fluidtank.fluids.{FluidAmountUtil, FluidConnection, fluidAccess}
import com.kotori316.fluidtank.tank.TileTank.{KEY_STACK_NAME, KEY_TANK, KEY_TIER}
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.Nameable
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluid
import org.jetbrains.annotations.{NotNull, Nullable}

class TileTank(var tier: Tier, t: BlockEntityType[_ <: TileTank], p: BlockPos, s: BlockState)
  extends BlockEntity(t, p, s) with Nameable {

  def this(p: BlockPos, s: BlockState) = {
    this(Tier.INVALID, ???, p, s)
  }

  def this(tier: Tier, p: BlockPos, s: BlockState) = {
    this(tier, ???, p, s)
  }

  private var connection: FluidConnection = new FluidConnection(Nil)
  private var tank: Tank[Fluid] = Tank(FluidAmountUtil.EMPTY, GenericUnit(tier.getCapacity))
  private var customName: Option[Component] = None

  def setConnection(c: FluidConnection): Unit = this.connection = c

  def getConnection: FluidConnection = this.connection

  def setTank(tank: Tank[Fluid]): Unit = {
    this.tank = tank
  }

  def getTank: Tank[Fluid] = this.tank

  // Override of BlockEntity
  override def load(tag: CompoundTag): Unit = {
    super.load(tag)
    this.tank = TankUtil.load(tag.getCompound(KEY_TANK))
    this.tier = Tier.valueOf(tag.getString(KEY_TIER))
    this.customName = Option.when(tag.contains(KEY_STACK_NAME))(
      Component.Serializer.fromJson(tag.getString(KEY_STACK_NAME))
    )
  }

  override def saveAdditional(tag: CompoundTag): Unit = {
    tag.put(KEY_TANK, TankUtil.save(this.tank))
    tag.putString(KEY_TIER, this.tier.name())
    this.customName.foreach(c => tag.putString(KEY_STACK_NAME, Component.Serializer.toJson(c)))
    super.saveAdditional(tag)
  }

  override def getUpdateTag: CompoundTag = this.saveWithoutMetadata()

  override def getUpdatePacket: ClientboundBlockEntityDataPacket = ClientboundBlockEntityDataPacket.create(this)

  // Override of Nameable
  @NotNull
  override def getName: Component = this.customName.getOrElse(Component.literal(this.tier.toString + " Tank"))

  @Nullable
  override def getCustomName: Component = this.customName.orNull

  def setCustomName(@Nullable customName: Component): Unit = {
    this.customName = Option(customName)
  }
}

object TileTank {
  final val KEY_TANK = "tank" // Tag map
  final val KEY_TIER = "tier" // Tag map provided in Tier class (Actually, String)
  final val KEY_CAPACITY = "capacity" // Long
  final val KEY_STACK_NAME = "stackName" // String parsed in Text

}
