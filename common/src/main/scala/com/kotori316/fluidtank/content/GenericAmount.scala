package com.kotori316.fluidtank.content

import cats.Hash
import cats.implicits.{catsSyntaxEq, catsSyntaxGroup, catsSyntaxSemigroup}
import com.kotori316.fluidtank.content.Implicits._
import net.minecraft.nbt.CompoundTag

case class GenericAmount[ContentType](content: ContentType, amount: GenericUnit, nbt: Option[CompoundTag])
                                     (implicit access: GenericAccess[ContentType], contentHash: Hash[ContentType]) {
  final def setAmount(newAmount: GenericUnit): GenericAmount[ContentType] = new GenericAmount[ContentType](this.content, newAmount, this.nbt)

  final def isEmpty: Boolean = access.isEmpty(this.content) || this.amount.value <= 0

  final def nonEmpty: Boolean = !isEmpty

  final def write(tag: CompoundTag): CompoundTag = access.write(this, tag)

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
}
