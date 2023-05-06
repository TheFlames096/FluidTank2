package com.kotori316.fluidtank.tank

import com.kotori316.fluidtank.BeforeMC
import com.kotori316.fluidtank.connection.Connection
import com.kotori316.fluidtank.contents.{GenericUnit, Tank}
import com.kotori316.fluidtank.fluids._
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.{Nested, Test}

class TileTankTest extends BeforeMC {
  private val tankBlock = new BlockTank(Tier.WOOD)

  def createTile(tier: Tier, pos: BlockPos): TileTank = new TileTank(tier, null, pos, tankBlock.defaultBlockState())

  @Test
  def create(): Unit = {
    val tile = createTile(Tier.WOOD, BlockPos.ZERO)
    assertNotNull(tile)
  }

  @Nested
  class InitialTest {
    @Test
    def connection(): Unit = {
      val tile = createTile(Tier.WOOD, BlockPos.ZERO)
      val c = tile.getConnection
      assertTrue(c.isDummy)
    }

    @Test
    def defaultTank(): Unit = {
      val tile = createTile(Tier.WOOD, BlockPos.ZERO)
      assertTrue(tile.getTank.isEmpty)
      assertEquals(GenericUnit.ZERO, tile.getConnection.amount)
      assertEquals(Option.empty, tile.getConnection.getContent)
    }

    @Test
    def initialCapacity(): Unit = {
      val tile = createTile(Tier.WOOD, BlockPos.ZERO)
      assertEquals(GenericUnit.fromForge(4000), tile.getTank.capacity)
      assertNotEquals(GenericUnit.fromForge(4000), tile.getConnection.capacity)
    }
  }

  @Nested
  class EstablishConnection {
    @Test
    def createConnection(): Unit = {
      val tile1 = createTile(Tier.WOOD, BlockPos.ZERO)
      val tile2 = createTile(Tier.STONE, BlockPos.ZERO.above())
      Connection.createAndInit(Seq(tile1, tile2))

      assertSame(tile1.getConnection, tile2.getConnection)
      val c = tile1.getConnection
      assertEquals(2, c.getHandler.getTank.size)
    }

    @Test
    def connectionAmount(): Unit = {
      val tile1 = createTile(Tier.WOOD, BlockPos.ZERO)
      val tile2 = createTile(Tier.STONE, BlockPos.ZERO.above())
      Connection.createAndInit(Seq(tile1, tile2))
      val c = tile1.getConnection

      assertEquals(GenericUnit.fromForge(20000), c.capacity)
    }

    @Test
    def move(): Unit = {
      val tile1 = createTile(Tier.WOOD, BlockPos.ZERO)
      val tile2 = createTile(Tier.STONE, BlockPos.ZERO.above())
      val fluid = FluidAmountUtil.BUCKET_WATER.setAmount(GenericUnit.fromForge(3000))
      tile2.setTank(tile2.getTank.copy(content = fluid))
      Connection.createAndInit(Seq(tile1, tile2))
      val c = tile1.getConnection
      assertEquals(
        Option(fluid),
        c.getContent,
      )

      assertEquals(fluid, tile1.getTank.content)
      assertTrue(tile2.getTank.isEmpty)
    }
  }

  @Nested
  class FillDrainTest {
    @Test
    def fill1(): Unit = {
      val tile1 = createTile(Tier.WOOD, BlockPos.ZERO)
      val tile2 = createTile(Tier.STONE, BlockPos.ZERO.above())
      val fluid = FluidAmountUtil.BUCKET_WATER.setAmount(GenericUnit.fromForge(3000))
      tile1.setTank(tile1.getTank.copy(content = fluid))
      Connection.createAndInit(Seq(tile1, tile2))

      val c = tile1.getConnection
      val filled = c.getHandler.fill(fluid, execute = false)
      assertEquals(fluid, filled)
      val filled2 = c.getHandler.fill(filled, execute = true)
      assertEquals(filled2, filled)
      assertEquals(Tank(FluidAmountUtil.BUCKET_WATER.setAmount(GenericUnit.fromForge(4000)), GenericUnit.fromForge(4000)), tile1.getTank)
      assertEquals(Tank(FluidAmountUtil.BUCKET_WATER.setAmount(GenericUnit.fromForge(2000)), GenericUnit.fromForge(16000)), tile2.getTank)
    }

    @Test
    def drain1(): Unit = {
      val tile1 = createTile(Tier.WOOD, BlockPos.ZERO)
      val tile2 = createTile(Tier.STONE, BlockPos.ZERO.above())
      val fluid = FluidAmountUtil.BUCKET_WATER.setAmount(GenericUnit.fromForge(3000))
      tile1.setTank(tile1.getTank.copy(content = fluid))
      Connection.createAndInit(Seq(tile1, tile2))
      val c = tile1.getConnection

      val drained = c.getHandler.drain(FluidAmountUtil.BUCKET_WATER, execute = false)
      assertEquals(FluidAmountUtil.BUCKET_WATER, drained)
      val d2 = c.getHandler.drain(drained, execute = true)
      assertEquals(drained, d2)
      assertEquals(Tank(FluidAmountUtil.BUCKET_WATER.setAmount(GenericUnit.fromForge(2000)), GenericUnit.fromForge(4000)), tile1.getTank)
      assertTrue(tile2.getTank.isEmpty)
    }
  }

  @Nested
  class NameTest {
    @Test
    def noCustomName(): Unit = {
      val tile = createTile(Tier.WOOD, BlockPos.ZERO)
      assertNotNull(tile.getName)
      assertNull(tile.getCustomName)
    }

    @Test
    def setName(): Unit = {
      val name = Component.literal("CustomName")
      val tile = createTile(Tier.WOOD, BlockPos.ZERO)
      tile.setCustomName(name)
      assertEquals(name, tile.getName)
      assertEquals(name, tile.getCustomName)
      assertEquals(name, tile.getDisplayName)
    }
  }
}
