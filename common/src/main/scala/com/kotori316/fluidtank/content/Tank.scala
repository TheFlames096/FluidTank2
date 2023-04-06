package com.kotori316.fluidtank.content

import java.util.Objects

case class Tank[A](content: GenericAmount[A], capacity: GenericUnit) {
  def amount: GenericUnit = content.amount

  def isEmpty: Boolean = content.isEmpty

  override def hashCode(): Int = Objects.hash(content, capacity.value)

  override def equals(obj: Any): Boolean = obj match {
    case t: Tank[_] => this.content == t.content && this.capacity.value == t.capacity.value
    case _ => false
  }

  override def toString: String = s"Tank{content=$content, capacity=${capacity.value}}"
}
 