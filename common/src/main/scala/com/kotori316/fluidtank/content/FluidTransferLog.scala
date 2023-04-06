package com.kotori316.fluidtank.content

import cats.Show

sealed trait FluidTransferLog {
  def logString: String

  override final def toString: String = this.logString
}

object FluidTransferLog {

  case class FillFluid[A](toFill: GenericAmount[A], filled: GenericAmount[A], before: Tank[A], after: Tank[A]) extends FluidTransferLog {
    override def logString: String = s"FillFluid{Filled=$filled, ToFill=$toFill, Before={${before.content}}, After={${after.content}}}"
  }

  case class FillFailed[A](fluid: GenericAmount[A], tank: Tank[A]) extends FluidTransferLog {
    override def logString: String = s"FillFailed{FailedToFill=$fluid, Tank={${tank.content}}}"
  }

  case class FillAll[A](fluid: GenericAmount[A], tank: Tank[A]) extends FluidTransferLog {
    override def logString: String = s"FillAll{Filled=$fluid, Tank={${tank.content}}}"
  }

  case class DrainFluid[A](toDrain: GenericAmount[A], drained: GenericAmount[A], before: Tank[A], after: Tank[A]) extends FluidTransferLog {
    override def logString: String = s"DrainFluid{Drained=$drained, ToDrain=$toDrain, Before={${before.content}}, After={${after.content}}}"
  }

  case class DrainFailed[A](fluid: GenericAmount[A], tank: Tank[A]) extends FluidTransferLog {
    override def logString: String = s"DrainFailed{ToDrain=$fluid, Tank={${tank.content}}}"
  }

  implicit val showFluidTransferLog: Show[FluidTransferLog] = _.logString
}
