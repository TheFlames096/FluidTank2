package com.kotori316.fluidtank.contents

import com.kotori316.fluidtank.contents.Operations.TankOperation

class VoidTank[A](e: GenericAmount[A], c: GenericUnit) extends Tank(e.createEmpty, c) {
  /**
   * Changing the content is not allowed
   *
   * @return new void tank with new capacity
   */
  override def copy(ignored: GenericAmount[A], capacity: GenericUnit): Tank[A] =
    new VoidTank(this.content, capacity)

  override def fillOp: TankOperation[A] = Operations.fillVoidOp(this)

  // This tank is always empty, so default implementation is enough.
  override def drainOp: TankOperation[A] = super.drainOp
}

object VoidTank {
  def apply[A](implicit access: GenericAccess[A]): VoidTank[A] =
    new VoidTank(access.newInstance(access.empty, GenericUnit.ZERO, None), GenericUnit.ZERO)
}
