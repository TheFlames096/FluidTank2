package com.kotori316.fluidtank.fluids;

import cats.implicits.catsSyntaxEq
import com.kotori316.fluidtank.BeforeMC
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.material.Fluids
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test;

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
}
