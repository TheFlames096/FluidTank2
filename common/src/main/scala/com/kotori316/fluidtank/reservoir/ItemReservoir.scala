package com.kotori316.fluidtank.reservoir

import cats.implicits.catsSyntaxGroup
import com.kotori316.fluidtank.contents.{GenericUnit, Tank, TankUtil}
import com.kotori316.fluidtank.fluids.{FluidAmount, FluidAmountUtil, FluidLike, PlatformFluidAccess, PotionType, VanillaFluid, VanillaPotion, fluidAccess}
import com.kotori316.fluidtank.tank.{Tier, TileTank}
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.alchemy.PotionUtils
import net.minecraft.world.item.{Item, ItemStack, ItemUtils, Rarity, TooltipFlag, UseAnim}
import net.minecraft.world.level.block.BucketPickup
import net.minecraft.world.level.{ClipContext, Level}
import net.minecraft.world.phys.{BlockHitResult, HitResult}
import net.minecraft.world.{InteractionHand, InteractionResultHolder}

import java.util
import java.util.Locale

class ItemReservoir(val tier: Tier) extends Item(new Item.Properties().stacksTo(1)) {
  override def toString: String = s"ItemReservoir(${tier.name().toLowerCase(Locale.ROOT)})"

  override def getUseAnimation(stack: ItemStack): UseAnim = {
    getTank(stack).content.content match {
      case v: VanillaPotion if v.potionType == PotionType.NORMAL => UseAnim.DRINK
      case _ => super.getUseAnimation(stack)
    }
  }

  override def getUseDuration(stack: ItemStack): Int = {
    getTank(stack).content.content match {
      case v: VanillaPotion if v.potionType == PotionType.NORMAL => Item.EAT_DURATION
      case _ => super.getUseDuration(stack)
    }
  }

  override def use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder[ItemStack] = {
    val stack = player.getItemInHand(usedHand)
    val content = getTank(stack).content
    content.content match {
      case v: VanillaPotion if v.potionType == PotionType.NORMAL => ItemUtils.startUsingInstantly(level, player, usedHand);
      case _: VanillaFluid =>
        val hitResult = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY)
        if (hitResult.getType != HitResult.Type.BLOCK) {
          InteractionResultHolder.pass(stack)
        } else {
          fillOrDrainFluidInLevel(hitResult, stack, level, player, content, usedHand)
        }
      case _ => super.use(level, player, usedHand)
    }
  }

  override def finishUsingItem(stack: ItemStack, level: Level, livingEntity: LivingEntity): ItemStack = {
    val tank = getTank(stack)
    val content = tank.content
    content.content match {
      case _: VanillaPotion if content.hasOneBottle =>
        val effects = PotionUtils.getAllEffects(content.nbt.orNull)
        effects.forEach { e =>
          if (e.getEffect.isInstantenous) {
            e.getEffect.applyInstantenousEffect(livingEntity, livingEntity, livingEntity, e.getAmplifier, 1.0)
          } else {
            livingEntity.addEffect(e)
          }
        }
        val newTank = tank.copy(content = content.setAmount(content.amount |-| GenericUnit.ONE_BOTTLE))
        this.saveTank(stack, newTank)
        stack
      case _ => stack
    }
  }

  override def getRarity(stack: ItemStack): Rarity = {
    if (stack.getTagElement(TileTank.KEY_TANK) != null) Rarity.UNCOMMON
    else super.getRarity(stack)
  }

  override def appendHoverText(stack: ItemStack, level: Level, tooltip: util.List[Component], isAdvanced: TooltipFlag): Unit = {
    super.appendHoverText(stack, level, tooltip, isAdvanced)
    val tank = getTank(stack)
    if (tank.isEmpty) {
      tooltip.add(Component.translatable("fluidtank.waila.capacity", GenericUnit.asForgeFromBigInt(tier.getCapacity)))
    } else {
      val fluid = tank.content
      val capacity = tank.capacity
      tooltip.add(Component.translatable("fluidtank.waila.short",
        PlatformFluidAccess.getInstance().getDisplayName(fluid), fluid.amount.asDisplay, capacity.asDisplay))
    }
  }

  def getTank(stack: ItemStack): Tank[FluidLike] = {
    val tag = stack.getTagElement(TileTank.KEY_TANK)
    if (tag == null) {
      Tank(FluidAmountUtil.EMPTY, GenericUnit(tier.getCapacity))
    } else {
      TankUtil.load(tag)
    }
  }

  def saveTank(stack: ItemStack, tank: Tank[FluidLike]): Unit = {
    if (tank.isEmpty) {
      stack.removeTagKey(TileTank.KEY_TANK)
    } else {
      stack.addTagElement(TileTank.KEY_TANK, TankUtil.save(tank))
    }
  }

  private def fillOrDrainFluidInLevel(hitResult: BlockHitResult, stack: ItemStack, level: Level, player: Player, content: FluidAmount, hand: InteractionHand): InteractionResultHolder[ItemStack] = {
    val hitPos = hitResult.getBlockPos
    val hitFace = hitResult.getDirection
    if (level.mayInteract(player, hitPos) && player.mayUseItemAt(hitPos.relative(hitFace), hitFace, stack)) {
      val blockState = level.getBlockState(hitPos)
      val simulateFluid = FluidAmountUtil.from(level.getFluidState(hitPos).getType, GenericUnit.ONE_BUCKET)
      blockState.getBlock match {
        case pickUp: BucketPickup if content.isEmpty || content.contentEqual(simulateFluid) =>
          val simulation = PlatformFluidAccess.getInstance().fillItem(simulateFluid, stack, player, hand, false)
          if (simulation.moved().nonEmpty) {
            val picked = pickUp.pickupBlock(null, level, hitPos, blockState)
            val actualFluid = PlatformFluidAccess.getInstance().getFluidContained(picked)
            val result = PlatformFluidAccess.getInstance().fillItem(actualFluid, stack, player, hand, true)
            InteractionResultHolder.sidedSuccess(result.toReplace, level.isClientSide)
          } else {
            InteractionResultHolder.pass(stack)
          }
        case _ =>
          InteractionResultHolder.pass(stack)
      }
    } else {
      InteractionResultHolder.fail(stack)
    }
  }
}
