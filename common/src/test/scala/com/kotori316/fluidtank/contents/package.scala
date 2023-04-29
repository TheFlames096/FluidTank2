package com.kotori316.fluidtank

import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation

import scala.reflect.ClassTag

package object contents {
  implicit final val gaString: GenericAccess[String] = new StringGenericAccess

  private final class StringGenericAccess extends GenericAccess[String] {
    def isEmpty(a: String): Boolean = a.isEmpty

    def isGaseous(a: String): Boolean = a.contains("gas")

    def getKey(a: String): ResourceLocation = new ResourceLocation(a)

    def empty: String = ""

    def write(amount: GenericAmount[String]): CompoundTag = {
      val tag = new CompoundTag()
      tag.putString("content", amount.content)
      tag.putString("amount", amount.amount.value.toString())
      tag
    }

    override def read(tag: CompoundTag): GenericAmount[String] = {
      val content = tag.getString("content")
      val amount = GenericUnit(BigInt(tag.getString("amount")))
      GenericAmount(content, amount, Option.empty)
    }

    def classTag: ClassTag[String] = implicitly[ClassTag[String]]
  }
}
