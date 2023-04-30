package com.kotori316.fluidtank.contents

import cats.kernel.CommutativeGroup
import org.jetbrains.annotations.VisibleForTesting

class GenericUnit private(val value: BigInt) extends AnyVal {
  def asFabric: Long = {
    if (this.value > GenericUnit.LONG_MAX) {
      Long.MaxValue
    } else {
      this.value.longValue
    }
  }

  def asForge: Int = {
    val inForge = this.value / 81
    if (inForge > GenericUnit.INT_MAX) {
      Int.MaxValue
    } else {
      inForge.intValue
    }
  }

  def asByteArray: Array[Byte] = this.value.toByteArray

  def asForgeDouble: Double = {
    this.value.doubleValue / 81d
  }
}

object GenericUnit {
  private final val FABRIC_ONE_BUCKET = 81000
  private final val FORGE_ONE_BUCKET = 1000
  private final val LONG_MAX = BigInt(Long.MaxValue)
  private final val INT_MAX = BigInt(Int.MaxValue)
  final val ZERO: GenericUnit = new GenericUnit(BigInt(0))
  final val ONE_BUCKET: GenericUnit = new GenericUnit(BigInt(FABRIC_ONE_BUCKET))
  final val MAX: GenericUnit = new GenericUnit(GenericUnit.LONG_MAX * BigInt(FABRIC_ONE_BUCKET))

  def fromFabric(value: Long): GenericUnit = new GenericUnit(BigInt(value))

  def fromForge(value: Long): GenericUnit = new GenericUnit(BigInt(value * 81))

  def fromByteArray(value: Array[Byte]): GenericUnit = new GenericUnit(BigInt(value))

  implicit final val groupGenericUnit: CommutativeGroup[GenericUnit] = new GroupGenericUnit
  implicit final val orderingGenericUnit: Ordering[GenericUnit] = Ordering.by(_.value)

  private class GroupGenericUnit extends CommutativeGroup[GenericUnit] {
    override def inverse(a: GenericUnit): GenericUnit = new GenericUnit(-a.value)

    override def empty: GenericUnit = new GenericUnit(BigInt(0))

    override def combine(x: GenericUnit, y: GenericUnit): GenericUnit = new GenericUnit(x.value + y.value)

    override def remove(x: GenericUnit, y: GenericUnit): GenericUnit = new GenericUnit(x.value - y.value)

    override def combineN(a: GenericUnit, n: Int): GenericUnit = new GenericUnit(a.value * n)
  }

  @VisibleForTesting
  private[contents] def apply(value: BigInt): GenericUnit = new GenericUnit(value)
}
