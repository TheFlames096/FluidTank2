package com.kotori316.fluidtank

import cats.data.Chain
import cats.{Monad, MonoidK}
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation

import scala.reflect.ClassTag

package object contents {
  implicit final val gaString: GenericAccess[String] = new StringGenericAccess

  def createTanks(settings: (String, Long, Long)*): Chain[Tank[String]] = {
    val monoidK = implicitly[MonoidK[Chain]]
    val monad = implicitly[Monad[Chain]]
    monoidK.combineAllK(settings.map { case (value, l, l1) => Tank(GenericAmount(value, GenericUnit(l), None), GenericUnit(l1)) }
      .map(t => monad.pure(t)))
  }

  private final class StringGenericAccess extends GenericAccess[String] {
    def isEmpty(a: String): Boolean = a.isEmpty

    def isGaseous(a: String): Boolean = a.contains("gas")

    def getKey(a: String): ResourceLocation = new ResourceLocation(a)

    def empty: String = ""

    def write(amount: GenericAmount[String]): CompoundTag = {
      val tag = new CompoundTag()
      tag.putString("content", amount.content)
      tag.putString("amount", amount.amount.value.toString())
      amount.nbt.foreach(t => tag.put("tag", t))
      tag
    }

    override def read(tag: CompoundTag): GenericAmount[String] = {
      val content = tag.getString("content")
      val amount = GenericUnit(BigInt(tag.getString("amount")))
      val nbt = Option.when(tag.contains("tag"))(tag.getCompound("tag"))
      GenericAmount(content, amount, nbt)
    }

    def classTag: ClassTag[String] = implicitly[ClassTag[String]]
  }
}
