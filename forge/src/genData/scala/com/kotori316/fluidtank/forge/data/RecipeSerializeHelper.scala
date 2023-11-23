package com.kotori316.fluidtank.forge.data

import com.google.gson.JsonObject
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger
import net.minecraft.data.recipes.{FinishedRecipe, RecipeBuilder, RecipeOutput, SpecialRecipeBuilder}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.{CraftingRecipe, RecipeSerializer}

case class RecipeSerializeHelper(recipe: FinishedRecipe,
                                 conditions: List[PlatformedCondition] = Nil,
                                 saveName: ResourceLocation = null,
                                 advancement: AdvancementSerializeHelper = AdvancementSerializeHelper(),
                                ) {
  def this(c: RecipeBuilder, saveName: ResourceLocation) = {
    this(RecipeSerializeHelper.getConsumeValue(c), saveName = saveName)
  }

  def addCondition(condition: PlatformedCondition): RecipeSerializeHelper =
    copy(conditions = condition :: this.conditions)

  def addTagCondition(ingredientHelper: RecipeIngredientHelper): RecipeSerializeHelper =
    addCondition(PlatformedCondition.Tag(ingredientHelper))

  def build: JsonObject = {
    val o = recipe.serializeRecipe()
    FluidTankDataProvider.addPlatformConditions(o, this.conditions)
    o
  }

  def location: ResourceLocation = if (saveName == null) recipe.id() else saveName

  def addItemCriterion(item: Item): RecipeSerializeHelper =
    this.copy(advancement = advancement.addItemCriterion(item))

  def addItemCriterion(ingredientHelper: RecipeIngredientHelper): RecipeSerializeHelper =
    this.copy(advancement = advancement.addItemCriterion(ingredientHelper))
}

object RecipeSerializeHelper {
  def by(c: RecipeBuilder, saveName: ResourceLocation = null): RecipeSerializeHelper = new RecipeSerializeHelper(c, saveName)

  def bySpecial(serializer: RecipeSerializer[? <: CraftingRecipe], recipeId: String, saveName: ResourceLocation = null): RecipeSerializeHelper = {
    val c = SpecialRecipeBuilder.special(serializer)
    var t: FinishedRecipe = null
    c.save(new RecipeOutput {
      override def accept(arg: FinishedRecipe): Unit = t = arg

      override def advancement(): Advancement.Builder = Advancement.Builder.recipeAdvancement()
    }, recipeId)
    new RecipeSerializeHelper(t, Nil, saveName)
  }

  private def getConsumeValue(c: RecipeBuilder): FinishedRecipe = {
    val fixed: RecipeBuilder = c.unlockedBy("dummy", RecipeUnlockedTrigger.unlocked(new ResourceLocation("dummy:dummy")))
    var t: FinishedRecipe = null
    fixed.save(new RecipeOutput {
      override def accept(arg: FinishedRecipe): Unit = t = arg

      override def advancement(): Advancement.Builder = Advancement.Builder.recipeAdvancement()
    })
    t
  }

}
