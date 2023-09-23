package com.kotori316.fluidtank.forge.data

import com.kotori316.fluidtank.FluidTankCommon
import com.kotori316.fluidtank.forge.recipe.TierRecipeForge
import com.kotori316.fluidtank.forge.{FluidTank, data}
import com.kotori316.fluidtank.tank.Tier
import net.minecraft.data.recipes.{RecipeCategory, ShapedRecipeBuilder, ShapelessRecipeBuilder}
import net.minecraft.data.{CachedOutput, DataGenerator, DataProvider}
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient
import net.minecraftforge.common.Tags

import java.util.concurrent.CompletableFuture
import scala.jdk.javaapi.CollectionConverters

class RecipeProvider(gen: DataGenerator) extends DataProvider {
  override def run(output: CachedOutput): CompletableFuture[?] = {
    val outputWork = for {
      recipe <- getRecipes
      future <- Seq(saveRecipe(output, recipe), saveAdvancement(output, recipe))
    } yield future

    CompletableFuture.allOf(outputWork *)
  }

  private def saveRecipe(output: CachedOutput, recipe: RecipeSerializeHelper): CompletableFuture[?] = {
    val location = recipe.location
    val out = gen.getPackOutput.getOutputFolder.resolve(s"data/${location.getNamespace}/recipes/${location.getPath}.json")
    DataProvider.saveStable(output, recipe.build, out)
  }

  private def saveAdvancement(output: CachedOutput, recipe: RecipeSerializeHelper): CompletableFuture[?] = {
    val location = recipe.location
    val out = gen.getPackOutput.getOutputFolder.resolve(s"data/${location.getNamespace}/advancements/recipes/tank/${location.getPath}.json")
    DataProvider.saveStable(output, recipe.advancement.build(location), out)
  }

  def getRecipes: Seq[RecipeSerializeHelper] = {
    val woodTankBlock = FluidTank.TANK_MAP.get(Tier.WOOD).get()
    val glassSubItem = RecipeIngredientHelper.bothTag(Tags.Items.GLASS, "c:glass_blocks")
    val woodTank = RecipeSerializeHelper.by(ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, woodTankBlock)
        .define('x', Items.GLASS) // FIXME custom ingredient
        .define('p', ItemTags.LOGS)
        .pattern("x x")
        .pattern("xpx")
        .pattern("xxx"))
      .addTagCondition(glassSubItem)
      .addItemCriterion(Items.WATER_BUCKET)
      .addItemCriterion(glassSubItem)
    val obsidianSubItem = getSubItem(Tier.VOID)
    val voidTank = RecipeSerializeHelper.by(
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FluidTank.BLOCK_VOID_TANK.get())
          .define('o', Items.OBSIDIAN) // FIXME custom ingredient
          .define('t', woodTankBlock)
          .pattern("ooo")
          .pattern("oto")
          .pattern("ooo"))
      .addTagCondition(obsidianSubItem)
      .addItemCriterion(woodTankBlock.asItem())
    val normalTanks = Tier.values().filter(_.isNormalTankTier).filterNot(_ == Tier.WOOD)
      .map { t =>
        val subItem = getSubItem(t)
        data.RecipeSerializeHelper(new TierRecipeForge.TierFinishedRecipe(new ResourceLocation(FluidTankCommon.modId, t.getBlockName), t, subItem.ingredient))
          .addTagCondition(subItem)
          .addItemCriterion(subItem)
      }
    val reservoirs = CollectionConverters.asScala(FluidTank.RESERVOIR_MAP)
      .map { case (tier, v) =>
        val value = v.get()
        val tank = FluidTank.TANK_MAP.get(tier).get()
        RecipeSerializeHelper.by(ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, value)
            .requires(tank)
            .requires(Items.BUCKET)
            .requires(Items.BUCKET))
          .addItemCriterion(tank.asItem())
      }

    val cat = RecipeSerializeHelper.by(ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FluidTank.BLOCK_CAT.get())
        .define('p', Ingredient.of(Items.CHEST, Items.BARREL))
        .define('x', woodTankBlock)
        .pattern("x x")
        .pattern("xpx")
        .pattern("xxx"))
      .addItemCriterion(woodTankBlock.asItem())

    Seq(woodTank, voidTank, cat) ++ normalTanks ++ reservoirs
  }

  private def getSubItem(tier: Tier): RecipeIngredientHelper = {
    tier match {
      case Tier.WOOD => RecipeIngredientHelper.vanillaTag(ItemTags.LOGS)
      case Tier.STONE => RecipeIngredientHelper.forgeTagFabricItem(Tags.Items.STONE, Items.STONE)
      case Tier.IRON => RecipeIngredientHelper.bothTag(Tags.Items.INGOTS_IRON, "c:iron_ingots")
      case Tier.GOLD => RecipeIngredientHelper.bothTag(Tags.Items.INGOTS_GOLD, "c:gold_ingots")
      case Tier.DIAMOND => RecipeIngredientHelper.bothTag(Tags.Items.GEMS_DIAMOND, "c:diamonds")
      case Tier.EMERALD => RecipeIngredientHelper.bothTag(Tags.Items.GEMS_EMERALD, "c:emeralds")
      case Tier.STAR => RecipeIngredientHelper.forgeTagFabricItem(Tags.Items.NETHER_STARS, Items.NETHER_STAR)
      case Tier.VOID => RecipeIngredientHelper.forgeTagFabricItem(Tags.Items.OBSIDIAN, Items.OBSIDIAN)
      case Tier.COPPER => RecipeIngredientHelper.bothTag("forge:ingots/copper", "c:copper_ingots")
      case Tier.TIN => RecipeIngredientHelper.bothTag("forge:ingots/tin", "c:tin_ingots")
      case Tier.BRONZE => RecipeIngredientHelper.bothTag("forge:ingots/bronze", "c:bronze_ingots")
      case Tier.LEAD => RecipeIngredientHelper.bothTag("forge:ingots/lead", "c:lead_ingots")
      case Tier.SILVER => RecipeIngredientHelper.bothTag("forge:ingots/silver", "c:silver_ingots")
      case _ => throw new IllegalArgumentException("Sub item of %s is not found".formatted(tier))
    }
  }

  override def getName: String = "Recipe of FluidTank"
}
