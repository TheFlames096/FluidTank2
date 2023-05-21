package com.kotori316.fluidtank.forge.data

import com.google.gson.JsonObject
import net.minecraft.advancements.critereon.{InventoryChangeTrigger, ItemPredicate, RecipeUnlockedTrigger}
import net.minecraft.advancements.{Advancement, AdvancementRewards, CriterionTriggerInstance, RequirementsStrategy}
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Item
import net.minecraftforge.registries.ForgeRegistries

case class AdvancementSerializeHelper(criterionList: List[(String, CriterionTriggerInstance)] = Nil,
                                      conditions: List[PlatformedCondition] = Nil) {

  def addCriterion(name: String, criterion: CriterionTriggerInstance): AdvancementSerializeHelper =
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

  def build(location: ResourceLocation): JsonObject = {
    val builder = Advancement.Builder.advancement()
    builder.parent(new ResourceLocation("recipes/root"))
      .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(location))
      .rewards(AdvancementRewards.Builder.recipe(location))
      .requirements(RequirementsStrategy.OR)
    val obj = criterionList.foldLeft(builder) { case (b, (s, c)) => b.addCriterion(s, c) }
      .serializeToJson()
    obj.add("conditions", FluidTankDataProvider.makeForgeConditionArray(conditions))
    obj.add("fabric:load_conditions", FluidTankDataProvider.makeFabricConditionArray(conditions))
    obj
  }
}
