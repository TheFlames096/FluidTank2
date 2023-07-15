package com.kotori316.fluidtank.fluids

import cats.implicits.catsSyntaxEq
import com.kotori316.fluidtank.BeforeMC
import com.kotori316.fluidtank.contents.GenericUnit
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.material.Fluids
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FluidKeyTest extends BeforeMC {

  @Test
  def createKey(): Unit = {
    val key = FluidKey(Fluids.WATER, Option.empty)
    assertNotNull(key)
    assertFalse(key.isEmpty)
    assertTrue(key.isDefined)
  }

  @Test
  def equal1(): Unit = {
    val key1 = FluidKey(Fluids.WATER, Option.empty)
    val key2 = FluidKey(Fluids.WATER, Option.empty)
    assertEquals(key1, key2)
    assertTrue(key1 == key2)
    assertTrue(key1 === key2)
  }

  @Test
  def empty(): Unit = {
    val key = FluidKey(Fluids.EMPTY, Option.empty)
    assertTrue(key.isEmpty)
    assertFalse(key.isDefined)
  }

  @Test
  def tagChange(): Unit = {
    val tag = new CompoundTag()
    tag.putString("A", "a")
    val expected: CompoundTag = tag.copy()
    val key = FluidKey(Fluids.EMPTY, Option(tag))
    tag.putString("A", "b")

    assertEquals(FluidKey(Fluids.EMPTY, Option(expected)), key)
    assertNotEquals(FluidKey(Fluids.EMPTY, Option(tag)), key)
  }

  @Test
  def fromAmountEmpty(): Unit = {
    val key = FluidKey(Fluids.EMPTY, Option.empty)
    assertEquals(key, FluidKey.from(FluidAmountUtil.EMPTY))
  }

  @Test
  def fromAmount(): Unit = {
    val expected = FluidKey(Fluids.WATER, Option.empty)
    assertEquals(expected, FluidKey.from(FluidAmountUtil.BUCKET_WATER))
    assertEquals(expected, FluidKey.from(FluidAmountUtil.BUCKET_WATER.setAmount(GenericUnit.fromForge(2000))))
    assertNotEquals(expected, FluidKey.from(FluidAmountUtil.BUCKET_LAVA))
  }

  @Test
  def testToAmount(): Unit = {
    val key = FluidKey(Fluids.WATER, Option.empty)
    assertEquals(FluidAmountUtil.BUCKET_WATER, key.toAmount(GenericUnit.ONE_BUCKET))
    val two = GenericUnit.fromForge(2000)
    assertEquals(FluidAmountUtil.BUCKET_WATER.setAmount(two), key.toAmount(two))
  }
}
