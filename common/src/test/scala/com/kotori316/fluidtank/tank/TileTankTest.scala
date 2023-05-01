package com.kotori316.fluidtank.tank

import com.kotori316.fluidtank.BeforeMC
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.{Nested, Test}

class TileTankTest extends BeforeMC {

  def createTile(tier: Tier, pos: BlockPos): TileTank = new TileTank(tier, null, pos, Blocks.AIR.defaultBlockState())

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
  }
}
