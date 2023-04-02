package com.kotori316.fluidtank.content

import org.junit.jupiter.api.Assertions.{assertEquals, assertNotNull, assertTrue}
import org.junit.jupiter.api.{Nested, Test}

import scala.language.implicitConversions

class GenericAmountTest {

  implicit def int2GenericUnit(a: Int): GenericUnit = GenericUnit.fromForge(a)

  @Nested
  class BasicTest {
    @Test
    def createInstance(): Unit = {
      val amount = GenericAmount("", 0, None)
      assertNotNull(amount)
    }

    @Test
    def add1(): Unit = {
      val a = GenericAmount("a", 1, None)
      assertEquals(GenericAmount("a", 2, None), a + a)
    }

    @Test
    def add2(): Unit = {
      val a = GenericAmount("a", 1, None)
      assertEquals(GenericAmount("a", 3, None), a + a + a)
    }

    @Test
    def addNotSame(): Unit = {
      val a = GenericAmount("a", 1, None)
      val b = GenericAmount("b", 100, None)
      assertEquals(GenericAmount("a", 101, None), a + b)
      assertEquals(GenericAmount("b", 101, None), b + a)
    }

    @Test
    def minus1(): Unit = {
      val a = GenericAmount("a", 1, None)
      val subtract = a - a
      assertTrue(subtract.isEmpty)
      assertEquals("a", subtract.content)
    }
  }

  @Nested
  class EmptyTest {
    @Test
    def contentEmpty(): Unit = {
      val e = GenericAmount("", 100, None)
      assertTrue(e.isEmpty)
    }

    @Test
    def addEmpty(): Unit = {
      val a = GenericAmount("a", 1, None)
      val e = GenericAmount("", 100, None)
      assertEquals(GenericAmount("a", 101, None), a + e)
      assertEquals(GenericAmount("a", 101, None), e + a)
    }

    @Test
    def minusEmpty(): Unit = {
      val a = GenericAmount("a", 1000, None)
      val e = GenericAmount("", 100, None)
      assertEquals(GenericAmount("a", 900, None), a - e)
      assertEquals(GenericAmount("a", -900, None), e - a)
    }
  }
}
