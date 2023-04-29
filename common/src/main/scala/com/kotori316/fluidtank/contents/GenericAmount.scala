package com.kotori316.fluidtank.contents

import java.util.Objects

import cats.Hash
import cats.implicits.{catsSyntaxEq, catsSyntaxGroup, catsSyntaxSemigroup}
import com.kotori316.fluidtank.contents.Implicits._
import net.minecraft.nbt.CompoundTag

case class GenericAmount[ContentType](content: ContentType, amount: GenericUnit, nbt: Option[CompoundTag])
                                     (implicit access: GenericAccess[ContentType], contentHash: Hash[ContentType]) {
  final def setAmount(newAmount: GenericUnit): GenericAmount[ContentType] = new GenericAmount[ContentType](this.content, newAmount, this.nbt)

  final def isEmpty: Boolean = access.isEmpty(this.content) || this.amount.value <= 0

  final def nonEmpty: Boolean = !isEmpty

  final def getTag: CompoundTag = access.write(this)

  final def +(that: GenericAmount[ContentType]): GenericAmount[ContentType] = this add that

  final def add(that: GenericAmount[ContentType]): GenericAmount[ContentType] = {
    val added = this.amount |+| that.amount
    if (this.isEmpty) that.setAmount(added)
    else this.setAmount(added)
  }

  final def -(that: GenericAmount[ContentType]): GenericAmount[ContentType] = this minus that

  final def minus(that: GenericAmount[ContentType]): GenericAmount[ContentType] = {
    val subtracted = this.amount |-| that.amount
    if (this.isEmpty) that.setAmount(subtracted)
    else this.setAmount(subtracted)
  }

  final def contentEqual(that: GenericAmount[ContentType]): Boolean =
    this.content === that.content && this.nbt === that.nbt

  override final def equals(obj: Any): Boolean = obj match {
    case that: GenericAmount[_] =>
      val c = this.access.classTag
      that.content match {
        case c(content) => this.content === content && this.nbt === that.nbt && this.amount.value === that.amount.value
        case _ => false
      }
    case _ => false
  }

  override def hashCode(): Int = Objects.hash(this.content, this.amount.value, this.nbt)

  override def toString: String = s"GenericAmount{content=$content, amount=${amount.value}, nbt=$nbt}"
}
