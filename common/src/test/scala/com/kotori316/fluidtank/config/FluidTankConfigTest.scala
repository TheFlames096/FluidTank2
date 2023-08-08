package com.kotori316.fluidtank.config

import cats.data.NonEmptyChain
import cats.kernel.Eq
import com.google.gson.{GsonBuilder, JsonObject}
import com.kotori316.fluidtank.tank.Tier
import org.junit.jupiter.api.{Assertions, Nested, Test}

class FluidTankConfigTest {
  private final val gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

  @Test
  def loadFromJson(): Unit = {
    // language=json
    val jsonString =
      """{
        |  "renderLowerBound": 0.2,
        |  "renderUpperBound": 0.8,
        |  "debug": false,
        |  "capacities": {
        |    "invalid": "162000",
        |    "wood": "162000",
        |    "stone": "162000",
        |    "iron": "162000",
        |    "gold": "162000",
        |    "diamond": "162000",
        |    "emerald": "162000",
        |    "star": "162000",
        |    "creative": "162000",
        |    "void": "162000",
        |    "copper": "162000",
        |    "tin": "162000",
        |    "bronze": "162000",
        |    "lead": "162000",
        |    "silver": "162000"
        |  }
        |}
        |""".stripMargin
    val json = gson.fromJson(jsonString, classOf[JsonObject])
    val config = FluidTankConfig.getConfigDataFromJson(json)
    Assertions.assertTrue(config.isRight)

    val expected = ConfigData(Tier.values().map(t => t -> BigInt(162000)).toMap, 0.2, 0.8, debug = false)
    Assertions.assertEquals(expected, config.getOrElse(null))
  }

  @Test
  def loadFromJson2(): Unit = {
    // language=json
    val jsonString =
      """{
        |  "renderLowerBound": "0.2",
        |  "renderUpperBound": 0.8,
        |  "debug": "true",
        |  "capacities": {
        |    "invalid": "162000",
        |    "wood": 162000,
        |    "stone": "162000",
        |    "iron": "162000",
        |    "gold": "162000",
        |    "diamond": "162000",
        |    "emerald": "162000",
        |    "star": "162000",
        |    "creative": "162000",
        |    "void": "162000",
        |    "copper": "162000",
        |    "tin": "162000",
        |    "bronze": "162000",
        |    "lead": "162000",
        |    "silver": "162000"
        |  }
        |}
        |""".stripMargin
    val json = gson.fromJson(jsonString, classOf[JsonObject])
    val config = FluidTankConfig.getConfigDataFromJson(json)
    Assertions.assertTrue(config.isRight)

    val expected = ConfigData(Tier.values().map(t => t -> BigInt(162000)).toMap, 0.2, 0.8, debug = true)
    Assertions.assertEquals(expected, config.getOrElse(null))
  }

  @Nested
  class KeyErrorTest {
    @Test
    def noCapacity(): Unit = {
      // language=json
      val jsonString =
        """{
          |  "renderLowerBound": 0.2,
          |  "renderUpperBound": 0.8,
          |  "debug": false
          |}
          |""".stripMargin
      val json = gson.fromJson(jsonString, classOf[JsonObject])
      val config = FluidTankConfig.getConfigDataFromJson(json)
      Assertions.assertFalse(config.isRight)

      val expected = NonEmptyChain(FluidTankConfig.KeyNotFound("capacities"))
      config.left match {
        case Some(e) => Assertions.assertEquals(expected, e)
        case _ => Assertions.fail("Unreachable")
      }
    }

    @Test
    def noTier(): Unit = {
      // language=json
      val jsonString =
        """{
          |  "renderLowerBound": 0.2,
          |  "renderUpperBound": 0.8,
          |  "debug": false,
          |  "capacities": {
          |    "wood": "162000",
          |    "iron": "162000",
          |    "gold": "162000",
          |    "diamond": "162000",
          |    "emerald": "162000",
          |    "creative": "162000",
          |    "void": "162000",
          |    "copper": "162000",
          |    "tin": "162000",
          |    "lead": "162000",
          |    "silver": "162000"
          |  }
          |}
          |""".stripMargin
      val json = gson.fromJson(jsonString, classOf[JsonObject])
      val config = FluidTankConfig.getConfigDataFromJson(json)

      val expected = NonEmptyChain(
        FluidTankConfig.KeyNotFound("capacities.invalid"),
        FluidTankConfig.KeyNotFound("capacities.stone"),
        FluidTankConfig.KeyNotFound("capacities.star"),
        FluidTankConfig.KeyNotFound("capacities.bronze"),
      )
      config.left match {
        case Some(e) => Assertions.assertEquals(expected, e)
        case _ => Assertions.fail("Unreachable")
      }
    }

    @Test
    def noRenderBound(): Unit = {
      // language=json
      val jsonString =
        """{
          |  "capacities": {
          |    "invalid": "162000",
          |    "wood": "162000",
          |    "stone": "162000",
          |    "iron": "162000",
          |    "gold": "162000",
          |    "diamond": "162000",
          |    "emerald": "162000",
          |    "star": "162000",
          |    "creative": "162000",
          |    "void": "162000",
          |    "copper": "162000",
          |    "tin": "162000",
          |    "bronze": "162000",
          |    "lead": "162000",
          |    "silver": "162000"
          |  }
          |}
          |""".stripMargin
      val json = gson.fromJson(jsonString, classOf[JsonObject])
      val config = FluidTankConfig.getConfigDataFromJson(json)

      val expected = NonEmptyChain(
        FluidTankConfig.KeyNotFound("renderLowerBound"),
        FluidTankConfig.KeyNotFound("renderUpperBound"),
        FluidTankConfig.KeyNotFound("debug"),
      )
      config.left match {
        case Some(e) => Assertions.assertEquals(expected, e)
        case _ => Assertions.fail("Unreachable")
      }
    }
  }

  @Test
  def checkEq(): Unit = {
    val a1 = FluidTankConfig.Other("renderLowerBound", new NumberFormatException("a1"))
    val a2 = FluidTankConfig.Other("renderLowerBound", new NumberFormatException("a2"))
    val a3 = FluidTankConfig.Other("renderLowerBound", new IllegalArgumentException("a3"))

    val eq = Eq[FluidTankConfig.LoadError]
    Assertions.assertAll(
      () => Assertions.assertTrue(eq.eqv(a1, a2)),
      () => Assertions.assertTrue(eq.neqv(a1, a3)),
      () => Assertions.assertTrue(eq.neqv(a2, a3)),
    )
  }

  @Nested
  class ConvertErrorTest {
    @Test
    def invalidDouble(): Unit = {
      // language=json
      val jsonString =
        """{
          |  "renderLowerBound": "test",
          |  "renderUpperBound": "test2",
          |  "debug": true,
          |  "capacities": {
          |    "invalid": "162000",
          |    "wood": "162000",
          |    "stone": "162000",
          |    "iron": "162000",
          |    "gold": "162000",
          |    "diamond": "162000",
          |    "emerald": "162000",
          |    "star": "162000",
          |    "creative": "162000",
          |    "void": "162000",
          |    "copper": "162000",
          |    "tin": "162000",
          |    "bronze": "162000",
          |    "lead": "162000",
          |    "silver": "162000"
          |  }
          |}
          |""".stripMargin
      val json = gson.fromJson(jsonString, classOf[JsonObject])
      val config = FluidTankConfig.getConfigDataFromJson(json)

      val expected: NonEmptyChain[FluidTankConfig.LoadError] = NonEmptyChain(
        FluidTankConfig.Other("renderLowerBound", new NumberFormatException()),
        FluidTankConfig.Other("renderUpperBound", new NumberFormatException())
      )
      config.left match {
        case Some(e) =>
          Assertions.assertTrue(expected.toChain === e.toChain, s"$expected, $e")
        case _ => Assertions.fail("Unreachable")
      }
    }

    @Test
    def invalidCapacity(): Unit = {
      // language=json
      val jsonString =
        """{
          |  "renderLowerBound": 0.2,
          |  "renderUpperBound": 0.8,
          |  "debug": true,
          |  "capacities": {
          |    "invalid": "test",
          |    "wood": "1.6",
          |    "stone": "162000",
          |    "iron": "162000",
          |    "gold": "162000",
          |    "diamond": "162000",
          |    "emerald": "162000",
          |    "star": "162000",
          |    "creative": "162000",
          |    "void": "162000",
          |    "copper": "162000",
          |    "tin": "162000",
          |    "bronze": "162000",
          |    "lead": "162000",
          |    "silver": "162000"
          |  }
          |}
          |""".stripMargin
      val json = gson.fromJson(jsonString, classOf[JsonObject])
      val config = FluidTankConfig.getConfigDataFromJson(json)

      val expected: NonEmptyChain[FluidTankConfig.LoadError] = NonEmptyChain(
        FluidTankConfig.Other("capacities.invalid", new NumberFormatException()),
        FluidTankConfig.Other("capacities.wood", new NumberFormatException())
      )
      config.left match {
        case Some(e) =>
          Assertions.assertTrue(expected.toChain === e.toChain, s"$expected, $e")
        case _ => Assertions.fail("Unreachable")
      }
    }
  }
}
