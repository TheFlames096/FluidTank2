package com.kotori316.fluidtank.contents

import cats.Hash
import net.minecraft.nbt.CompoundTag

private[contents] object Implicits {

  implicit val hashCompoundTag: Hash[CompoundTag] = Hash.fromUniversalHashCode
}
