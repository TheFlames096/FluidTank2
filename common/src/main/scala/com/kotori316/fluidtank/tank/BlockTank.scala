package com.kotori316.fluidtank.tank

import cats.implicits.toShow
import com.kotori316.fluidtank.FluidTankCommon
import com.kotori316.fluidtank.MCImplicits.showPos
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.{BlockItem, Item, ItemStack}
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState, StateDefinition}
import net.minecraft.world.level.block.{Block, EntityBlock}
import net.minecraft.world.level.{BlockGetter, Level}
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.{CollisionContext, VoxelShape}
import net.minecraft.world.{InteractionHand, InteractionResult}
import org.jetbrains.annotations.Nullable

class BlockTank(val tier: Tier) extends Block(BlockBehaviour.Properties.of(FluidTankCommon.TANK_MATERIAL).strength(1f).dynamicShape()) with EntityBlock {

  protected def createInternalName: String = "tank_" + tier.toString.toLowerCase

  final val registryName = new ResourceLocation(FluidTankCommon.modId, createInternalName)
  registerDefaultState(this.getStateDefinition.any.setValue[TankPos, TankPos](TankPos.TANK_POS_PROPERTY, TankPos.SINGLE))
  final val itemBlock: ItemBlockTank = createTankItem()

  protected def createTankItem(): ItemBlockTank = new ItemBlockTank(this)

  override final def asItem(): Item = itemBlock

  override def toString: String = s"Block{$registryName}"

  override def newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = {
    new TileTank(tier, pos, state)
  }

  //noinspection ScalaDeprecation,deprecation
  override final def skipRendering(state: BlockState, adjacentBlockState: BlockState, side: Direction) = true

  //noinspection ScalaDeprecation,deprecation
  override def use(state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hit: BlockHitResult): InteractionResult = {
    InteractionResult.PASS
  }

  override def setPlacedBy(level: Level, pos: BlockPos, state: BlockState, @Nullable entity: LivingEntity, stack: ItemStack): Unit = {
    super.setPlacedBy(level, pos, state, entity, stack)
    level.getBlockEntity(pos) match {
      case tank: TileTank => if (!level.isClientSide) tank.onBlockPlacedBy()
      case tile => FluidTankCommon.LOGGER.error(FluidTankCommon.MARKER_TANK, "There is not TileTank at {}, but {}", pos.show, tile)
    }
  }

  //noinspection ScalaDeprecation,deprecation
  override final def hasAnalogOutputSignal(state: BlockState): Boolean = true

  //noinspection ScalaDeprecation,deprecation
  override final def getAnalogOutputSignal(blockState: BlockState, level: Level, pos: BlockPos): Int = {
    level.getBlockEntity(pos) match {
      case tileTank: TileTank => tileTank.getComparatorLevel
      case tile => FluidTankCommon.LOGGER.error(FluidTankCommon.MARKER_TANK, "There is not TileTank at {}, but {}", pos.show, tile); 0
    }
  }

  //noinspection ScalaDeprecation,deprecation
  override final def onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, moved: Boolean): Unit = {
    if (!state.is(newState.getBlock)) {
      level.getBlockEntity(pos) match {
        case tank: TileTank => tank.onDestroy()
        case tile => FluidTankCommon.LOGGER.error(FluidTankCommon.MARKER_TANK, "There is not TileTank at {}, but {}", pos.show, tile)
      }
      super.onRemove(state, level, pos, newState, moved)
    }
  }

  def saveTankNBT(tileEntity: BlockEntity, stack: ItemStack): Unit = {
    tileEntity match {
      case tank: TileTank =>
        if (!tank.getTank.isEmpty) stack.addTagElement(BlockItem.BLOCK_STATE_TAG, tank.saveWithoutMetadata())
        if (tank.hasCustomName) stack.setHoverName(tank.getCustomName)
      case _ => // should be unreachable
    }
  }

  override def getCloneItemStack(level: BlockGetter, pos: BlockPos, state: BlockState): ItemStack = {
    val stack = super.getCloneItemStack(level, pos, state)
    saveTankNBT(level.getBlockEntity(pos), stack)
    stack
  }

  //noinspection ScalaDeprecation,deprecation
  override def getShape(state: BlockState, worldIn: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = FluidTankCommon.TANK_SHAPE

  override def createBlockStateDefinition(builder: StateDefinition.Builder[Block, BlockState]): Unit = {
    super.createBlockStateDefinition(builder)
    builder.add(TankPos.TANK_POS_PROPERTY)
  }
}
