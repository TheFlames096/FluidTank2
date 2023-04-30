package com.kotori316.fluidtank.contents

import cats.data.Chain

abstract class ChainTanksHandler[T](pLimitOneFluid: Boolean) extends TanksHandler[T, Chain](pLimitOneFluid)
