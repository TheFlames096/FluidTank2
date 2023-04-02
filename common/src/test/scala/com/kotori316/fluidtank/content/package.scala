package com.kotori316.fluidtank

import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation

import scala.reflect.ClassTag

package object content {
  implicit final val gaString: GenericAccess[String] = new GenericAccess[String] {
    def isEmpty(a: String): Boolean = a.isEmpty

    def isGaseous(a: String): Boolean = a.contains("gas")

    def getKey(a: String): ResourceLocation = new ResourceLocation(a)

    def empty: String = ""

    def write(amount: GenericAmount[String], tag: CompoundTag): CompoundTag = {
      tag.putString("content", amount.content)
      tag.putString("amount", amount.amount.value.toString())
      tag
    }

    def classTag: ClassTag[String] = implicitly[ClassTag[String]]
  }
}
