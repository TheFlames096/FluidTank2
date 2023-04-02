package com.kotori316.fluidtank.content

import cats.Hash
import net.minecraft.nbt.CompoundTag

private[content] object Implicits {

  implicit val hashCompoundTag: Hash[CompoundTag] = Hash.fromUniversalHashCode
}
