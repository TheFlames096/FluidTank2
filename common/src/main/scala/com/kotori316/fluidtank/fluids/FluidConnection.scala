package com.kotori316.fluidtank.fluids

import com.kotori316.fluidtank.connection.{Connection, ConnectionHelper}
import com.kotori316.fluidtank.contents.{CreativeTank, GenericAmount, VoidTank}
import com.kotori316.fluidtank.tank.TileTank
import net.minecraft.core.BlockPos
import net.minecraft.world.level.material.Fluid

class FluidConnection(s: Seq[TileTank])(override implicit val helper: ConnectionHelper.Aux[TileTank, Fluid, FluidTanksHandler]) extends Connection[TileTank](s) {
  override val isDummy: Boolean = s.isEmpty

  def getHandler: FluidTanksHandler = this.handler
}

object FluidConnection {
  implicit final val fluidConnectionHelper: ConnectionHelper.Aux[TileTank, Fluid, FluidTanksHandler] = new FluidConnectionHelper

  private final class FluidConnectionHelper extends ConnectionHelper[TileTank] {
    override type Content = Fluid
    override type Handler = FluidTanksHandler
    override type ConnectionType = FluidConnection

    override def getPos(t: TileTank): BlockPos = t.getBlockPos

    override def isCreative(t: TileTank): Boolean = t.getTank.isInstanceOf[CreativeTank[_]]

    override def isVoid(t: TileTank): Boolean = t.getTank.isInstanceOf[VoidTank[_]]

    override def setChanged(t: TileTank): Unit = t.setChanged()

    override def getContentRaw(t: TileTank): GenericAmount[Fluid] = t.getTank.content

    override def defaultAmount: GenericAmount[Fluid] = FluidAmountUtil.EMPTY

    override def createHandler(s: Seq[TileTank]): FluidTanksHandler = new FluidTanksHandler(s)

    override def createConnection(s: Seq[TileTank]): FluidConnection = new FluidConnection(s)

    override def connectionSetter(connection: FluidConnection): TileTank => Unit = t => t.setConnection(connection)
  }
}
