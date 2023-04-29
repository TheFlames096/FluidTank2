package com.kotori316.fluidtank.contents

import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation

import scala.reflect.ClassTag

trait GenericAccess[A] {
  def isEmpty(a: A): Boolean

  def isGaseous(a: A): Boolean

  def getKey(a: A): ResourceLocation

  def empty: A

  def write(amount: GenericAmount[A]): CompoundTag

  def read(tag: CompoundTag): GenericAmount[A]

  def classTag: ClassTag[A]
}
