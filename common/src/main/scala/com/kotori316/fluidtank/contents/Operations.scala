package com.kotori316.fluidtank.contents

import cats.data.{Chain, ReaderWriterStateT}
import cats.implicits.{catsSyntaxEq, catsSyntaxGroup, toSemigroupKOps}
import cats.{Applicative, Foldable, Id, MonoidK}

import scala.math.Ordering.Implicits.infixOrderingOps

object Operations {

  type ListTankOperation[L[_], A] = ReaderWriterStateT[Id, TransferEnv, Chain[FluidTransferLog], GenericAmount[A], L[Tank[A]]]
  type TankOperation[A] = ListTankOperation[Id, A]

  def fillOp[A](tank: Tank[A]): TankOperation[A] = ReaderWriterStateT { (_, s) =>
    if (s.isEmpty) {
      // Nothing to fill
      (Chain(FluidTransferLog.FillFailed(s, tank)), s, Id(tank))
    } else if (tank.content.isEmpty || tank.content.contentEqual(s)) {
      val filled = (tank.capacity |-| tank.amount) min s.amount
      val filledStack = s.setAmount(filled)
      val newTank = tank.copy(tank.content + filledStack)
      val rest = s - filledStack
      (Chain(FluidTransferLog.FillFluid(s, filledStack, tank, newTank)), rest, Id(newTank))
    } else {
      // The content didn't match      
      (Chain(FluidTransferLog.FillFailed(s, tank)), s, Id(tank))
    }
  }

  def fillVoidOp[A](tank: Tank[A]): TankOperation[A] = ReaderWriterStateT { (_, s) =>
    // Tank doesn't change
    Id((Chain(FluidTransferLog.FillFluid(s, s, tank, tank)), s.createEmpty, Id(tank)))
  }

  def fillCreativeOp[A](tank: Tank[A]): TankOperation[A] = ReaderWriterStateT { (_, s) =>
    // Not `isEmpty` because 0 amount stack should be considered.
    if (s.isContentEmpty) {
      // Nothing to fill
      (Chain(FluidTransferLog.FillFailed(s, tank)), s, Id(tank))
    } else if (tank.content.isEmpty || tank.content.contentEqual(s)) {
      val newTank = tank.copy(s.setAmount(tank.capacity))
      val rest = if (tank.amount === tank.capacity) s else s.setAmount(GenericUnit.ZERO)
      (Chain(FluidTransferLog.FillFluid(s, s, tank, newTank)), rest, Id(newTank))
    } else {
      // The content didn't match
      (Chain(FluidTransferLog.FillFailed(s, tank)), s, Id(tank))
    }
  }

  def drainOp[A](tank: Tank[A]): TankOperation[A] = ReaderWriterStateT { (_, s) =>
    if (s.isEmpty || tank.isEmpty) {
      // Nothing to drain.
      (Chain(FluidTransferLog.DrainFailed(s, tank)), s, tank)
    } else if (s.contentEqual(tank.content)) {
      val drainAmount = tank.amount min s.amount
      val drainedStack = tank.content.setAmount(drainAmount)
      val newTank = tank.copy(tank.content - drainedStack)
      val rest = s - drainedStack
      (Chain(FluidTransferLog.DrainFluid(s, drainedStack, tank, newTank)), rest, newTank)
    } else {
      // Failed to drain
      (Chain(FluidTransferLog.DrainFailed(s, tank)), s, tank)
    }
  }

  def drainCreativeOp[A](tank: Tank[A]): TankOperation[A] = ReaderWriterStateT { (_, s) =>
    if (s.isEmpty || tank.isEmpty) {
      // Nothing to drain.
      (Chain(FluidTransferLog.DrainFailed(s, tank)), s, tank)
    } else if (s.contentEqual(tank.content)) {
      // Drain requested amount, and nothing changed.
      (Chain(FluidTransferLog.DrainFluid(s, s, tank, tank)), s.createEmpty, tank)
    } else {
      // Failed to drain
      (Chain(FluidTransferLog.DrainFailed(s, tank)), s, tank)
    }
  }

  private def opList[F[+_], A](opList: F[TankOperation[A]])(implicit applicative: Applicative[F], F: Foldable[F], monoidK: MonoidK[F]): ListTankOperation[F, A] = {
    val initialState: ListTankOperation[F, A] = ReaderWriterStateT.applyS(f => Id((Chain.empty, f, monoidK.empty)))
    F.foldLeft(opList, initialState) { (s, op) =>
      s.flatMap(filledTankList => op.map(t => filledTankList <+> applicative.pure(t)))
    }
  }

  def opList[F[+_], A](tanks: F[Tank[A]], opGetter: Tank[A] => TankOperation[A])(implicit applicative: Applicative[F], F: Foldable[F], monoidK: MonoidK[F]): ListTankOperation[F, A] =
    opList(applicative.map(tanks)(opGetter))

  def fillList[F[+_], A](tanks: F[Tank[A]])(implicit applicative: Applicative[F], F: Foldable[F], monoidK: MonoidK[F]): ListTankOperation[F, A] =
    opList(tanks, _.fillOp)

  def drainList[F[+_], A](tanks: F[Tank[A]])(implicit applicative: Applicative[F], F: Foldable[F], monoidK: MonoidK[F]): ListTankOperation[F, A] =
    opList(tanks, _.drainOp)
}
