package com.kotori316.fluidtank.content

import cats.Id
import cats.data.{Chain, ReaderWriterStateT}
import cats.implicits.catsSyntaxGroup

import scala.math.Ordering.Implicits.infixOrderingOps

object Operations {

  type ListTankOperation[L[_], A] = ReaderWriterStateT[Id, TransferEnv, Chain[FluidTransferLog], GenericAmount[A], L[Tank[A]]]
  type TankOperation[A] = ListTankOperation[Id, A]

  def fillOp[A](tank: Tank[A]): TankOperation[A] = ReaderWriterStateT { (_, s) =>
    if (s.isEmpty) {
      // Nothing to fill
      (Chain(FluidTransferLog.Empty(s, tank)), s, Id(tank))
    } else if (tank.content.isEmpty || tank.content.contentEqual(s)) {
      val filled = (tank.capacity |-| tank.amount) min s.amount
      val filledStack = s.setAmount(filled)
      val newTank = tank.copy(tank.content + filledStack)
      (Chain(FluidTransferLog.FillFluid(s, filledStack, tank, newTank)), s - filledStack, Id(newTank))
    } else {
      // The content didn't match      
      (Chain(FluidTransferLog.FillFailed(s, tank)), s, Id(tank))
    }
  }
}

