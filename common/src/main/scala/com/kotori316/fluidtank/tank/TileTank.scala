package com.kotori316.fluidtank.tank

import com.kotori316.fluidtank.contents.{GenericUnit, Tank}
import com.kotori316.fluidtank.fluids.FluidConnection._
import com.kotori316.fluidtank.fluids.{FluidAmountUtil, FluidConnection}
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.Nameable
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluid

class TileTank(var tier: Tier, t: BlockEntityType[_ <: TileTank], p: BlockPos, s: BlockState)
  extends BlockEntity(t, p, s) with Nameable {

  def this(p: BlockPos, s: BlockState) = {
    this(Tier.INVALID, ???, p, s)
  }

  def this(tier: Tier, p: BlockPos, s: BlockState) = {
    this(tier, ???, p, s)
  }

  private var connection: FluidConnection = new FluidConnection(Nil)
  private var tank: Tank[Fluid] = Tank(FluidAmountUtil.EMPTY, GenericUnit.fromBigInteger(tier.getCapacity))

  def setConnection(c: FluidConnection): Unit = {
    this.connection = c
  }

  def setTank(tank: Tank[Fluid]): Unit = {
    this.tank = tank
  }

  def getTank: Tank[Fluid] = this.tank

  override def getName: Component = ???
}
