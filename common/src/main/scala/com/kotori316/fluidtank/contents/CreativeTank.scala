package com.kotori316.fluidtank.contents

import com.kotori316.fluidtank.contents.Operations.TankOperation

final class CreativeTank[A](e: GenericAmount[A], c: GenericUnit) extends Tank(e, c) {
  override def copy(content: GenericAmount[A], capacity: GenericUnit): Tank[A] = {
    new CreativeTank(content, capacity)
  }

  override def fillOp: TankOperation[A] = Operations.fillCreativeOp(this)

  override def drainOp: TankOperation[A] = Operations.drainCreativeOp(this)
}
