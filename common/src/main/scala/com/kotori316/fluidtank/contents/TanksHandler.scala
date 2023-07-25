package com.kotori316.fluidtank.contents

import cats.data.Chain
import cats.implicits.*
import cats.{Foldable, Monad, MonoidK}
import com.kotori316.fluidtank.contents.Operations.ListTankOperation

import scala.math.Ordering.Implicits.infixOrderingOps

abstract class TanksHandler[T, ListType[+_]](limitOneFluid: Boolean)(implicit monoidK: MonoidK[ListType], monad: Monad[ListType], f: Foldable[ListType], reversible: Reversible[ListType]) {
  protected var tanks: ListType[Tank[T]] = monoidK.empty

  protected def hasCreative: Boolean = tanks.exists(_.isInstanceOf[CreativeTank[?]])

  /**
   * @return moved amount, meaning resource - left(not filled/drained)
   */
  private def action(op: ListTankOperation[ListType, T], resource: GenericAmount[T], execute: Boolean): GenericAmount[T] = {
    val (log, left, newTanks) = op.run(DefaultTransferEnv, resource)
    val moved = resource - left
    if (execute)
      updateTanks(newTanks)
    outputLog(log, execute)
    moved
  }

  protected def outputLog(logs: Chain[FluidTransferLog], execute: Boolean): Unit = {
  }

  protected def updateTanks(newTanks: ListType[Tank[T]]): Unit = this.tanks = newTanks

  def getSumOfCapacity: GenericUnit = this.tanks.foldMap(_.capacity) min GenericUnit.CREATIVE_TANK

  /**
   * @param resource to be filled
   * @param execute  true to change content, false to calculate result
   * @return filled amount
   */
  def fill(resource: GenericAmount[T], execute: Boolean): GenericAmount[T] = {
    if (limitOneFluid) {
      // if tank contains some fluids and they don't match resource, return empty
      if (tanks.exists(t => t.hasContent && !resource.contentEqual(t.content))) {
        return resource.createEmpty
      }
    }

    val op: ListTankOperation[ListType, T] = if (hasCreative) {
      Operations.opList(this.tanks, Operations.fillCreativeOp)
    } else if (resource.isGaseous) {
      // Fill from last
      Operations.fillList[ListType, T](reversible.reverse(this.tanks)).map(reversible.reverse)
    } else {
      // Fill from head
      Operations.fillList[ListType, T](this.tanks)
    }
    action(op, resource, execute)
  }

  /**
   *
   * @param resource to be drained. Should not be empty.
   * @param execute  true to change content, false to calculate result
   * @return drained amount
   */
  def drain(resource: GenericAmount[T], execute: Boolean): GenericAmount[T] = {
    val op: ListTankOperation[ListType, T] = if (hasCreative) {
      Operations.opList(this.tanks, Operations.drainCreativeOp)
    } else if (resource.isGaseous) {
      // Drain from head
      Operations.drainList[ListType, T](this.tanks)
    } else {
      // Drain from last
      Operations.drainList[ListType, T](reversible.reverse(this.tanks)).map(reversible.reverse)
    }
    action(op, resource, execute)
  }

  /**
   * Remove all contents and fill with given resource
   *
   * @param resource to be set
   * @return filled amount
   */
  def set(resource: GenericAmount[T]): GenericAmount[T] = {
    // Empty tanks
    // Just change the field is fine, as `updateTanks` will be called in filling
    this.tanks = monad.map(this.tanks) { tank => tank.copy(content = tank.content.createEmpty) }
    // Fill tanks
    fill(resource, execute = true)
  }
}
