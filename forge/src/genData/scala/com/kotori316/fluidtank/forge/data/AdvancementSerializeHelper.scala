package com.kotori316.fluidtank.forge.data

import com.google.gson.JsonObject
import net.minecraft.advancements.critereon.{InventoryChangeTrigger, ItemPredicate, RecipeUnlockedTrigger}
import net.minecraft.advancements.{Advancement, AdvancementRequirements, AdvancementRewards, Criterion, CriterionTriggerInstance}
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Item
import net.minecraftforge.registries.ForgeRegistries

import scala.annotation.nowarn

case class AdvancementSerializeHelper(criterionList: List[(String, Criterion[? <: CriterionTriggerInstance])] = Nil,
                                      conditions: List[PlatformedCondition] = Nil,
                                      isRecipe: Boolean = true) {

  def addCriterion(name: String, criterion: Criterion[? <: CriterionTriggerInstance]): AdvancementSerializeHelper =
    copy(criterionList = (name, criterion) :: criterionList)

  def addItemCriterion(item: Item): AdvancementSerializeHelper =
    addCriterion(s"has_${ForgeRegistries.ITEMS.getKey(item).getPath}", InventoryChangeTrigger.TriggerInstance.hasItems(item))

  def addItemCriterion(ingredientHelper: RecipeIngredientHelper): AdvancementSerializeHelper = {
    val criterionForge = {
      ingredientHelper.forgeTagLimit
        .map(tag => s"has_forge_${tag.getPath}" -> InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(ItemTags.create(tag)).build()))
        .getOrElse {
          val item = ingredientHelper.forgeIngredient.getItems.head.getItem
          s"has_item_${ForgeRegistries.ITEMS.getKey(item).getPath}" -> InventoryChangeTrigger.TriggerInstance.hasItems(item)
        }
    }
    val criterionFabric = {
      ingredientHelper.fabricTagLimit
        .map(tag => s"has_fabric_${tag.getPath}" -> InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(ItemTags.create(tag)).build()))
        .orElse {
          ingredientHelper.fabricIngredient.map(_.getItems.head.getItem)
            .map(item => s"has_item_${ForgeRegistries.ITEMS.getKey(item).getPath}" -> InventoryChangeTrigger.TriggerInstance.hasItems(item))
        }
    }
    val add = criterionForge :: criterionFabric.toList
    copy(criterionList = add ::: criterionList, conditions = PlatformedCondition.Tag(ingredientHelper) :: conditions)
  }

  def addCondition(condition: PlatformedCondition): AdvancementSerializeHelper =
    copy(conditions = condition :: conditions)

  //noinspection ScalaDeprecation,deprecation
  @nowarn
  def build(location: ResourceLocation): JsonObject = {
    val builder = if (this.isRecipe) Advancement.Builder.recipeAdvancement() else Advancement.Builder.advancement()
    builder.parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)
      .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(location))
      .rewards(AdvancementRewards.Builder.recipe(location))
      .requirements(AdvancementRequirements.Strategy.OR)
    val obj = criterionList.foldLeft(builder) { case (b, (s, c)) => b.addCriterion(s, c) }
      .build(location).value().serializeToJson()
    FluidTankDataProvider.addPlatformConditions(obj, this.conditions)
    obj
  }
}
