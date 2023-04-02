package com.kotori316.fluidtank.content

case class Tank[A](content: GenericAmount[A], capacity: GenericUnit) {
  def amount: GenericUnit = content.amount

  def isEmpty: Boolean = content.isEmpty
}
 