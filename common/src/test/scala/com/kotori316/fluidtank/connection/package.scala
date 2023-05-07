package com.kotori316.fluidtank

import cats.data.Chain
import com.kotori316.fluidtank.contents.{ChainTanksHandler, CreativeTank, GenericAmount, GenericUnit, Tank, VoidTank}
import net.minecraft.core.BlockPos

package object connection {

  case class StringTile(pos: BlockPos, var tank: Tank[String], var connection: Option[StringConnection])

  final class StringConnection(s: Seq[StringTile])(override implicit val helper: ConnectionHelper.Aux[StringTile, String, StringTanksHandler])
    extends Connection[StringTile](s) {
    def getHandler: StringTanksHandler = handler
  }

  final class StringTanksHandler(s: Seq[StringTile]) extends ChainTanksHandler[String](pLimitOneFluid = true) {
    this.tanks = Chain.fromSeq(s.map(_.tank))

    override def updateTanks(newTanks: Chain[Tank[String]]): Unit = {
      super.updateTanks(newTanks)
      s.zip(newTanks.toList).foreach {
        case (tile, value) => tile.tank = value
      }
    }

    def getTank: Chain[Tank[String]] = this.tanks
  }

  implicit final val stringConnectionHelper: ConnectionHelper.Aux[StringTile, String, StringTanksHandler] = new StringConnectionHelper

  private final class StringConnectionHelper extends ConnectionHelper[StringTile] {
    override type Content = String
    override type Handler = StringTanksHandler
    override type ConnectionType = StringConnection

    override def getPos(t: StringTile): BlockPos = t.pos

    override def isCreative(t: StringTile): Boolean = t.tank.isInstanceOf[CreativeTank[_]]

    override def isVoid(t: StringTile): Boolean = t.tank.isInstanceOf[VoidTank[_]]

    override def getContentRaw(t: StringTile): GenericAmount[String] = t.tank.content

    override def defaultAmount: GenericAmount[String] = GenericAmount("", GenericUnit.ZERO, None)

    override def createHandler(s: Seq[StringTile]): StringTanksHandler = new StringTanksHandler(s)

    override def createConnection(s: Seq[StringTile]): StringConnection = new StringConnection(s)

    override def connectionSetter(connection: StringConnection): StringTile => Unit = t => t.connection = Option(connection)
  }
}
