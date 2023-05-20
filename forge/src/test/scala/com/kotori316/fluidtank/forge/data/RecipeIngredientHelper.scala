package com.kotori316.fluidtank.forge.data

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.{ItemTags, TagKey}
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike

import scala.jdk.javaapi.CollectionConverters

case class RecipeIngredientHelper(forgeIngredient: Ingredient,
                                  fabricIngredient: Option[Ingredient],
                                  forgeTagLimit: Option[ResourceLocation],
                                  fabricTagLimit: Option[ResourceLocation],
                                 ) {
  def ingredient: Ingredient = {
    Ingredient.merge(CollectionConverters.asJava(Seq(forgeIngredient, fabricIngredient.getOrElse(Ingredient.EMPTY))))
  }
}

object RecipeIngredientHelper {
  def vanillaTag(key: TagKey[Item]): RecipeIngredientHelper = {
    RecipeIngredientHelper(Ingredient.of(key), None, None, None)
  }

  def item(item: ItemLike): RecipeIngredientHelper = {
    RecipeIngredientHelper(Ingredient.of(item), None, None, None)
  }

  def bothTag(forgeTag: TagKey[Item], fabricTag: String): RecipeIngredientHelper = {
    val fTag = ItemTags.create(new ResourceLocation(fabricTag))
    RecipeIngredientHelper(
      Ingredient.of(forgeTag), Some(Ingredient.of(fTag)),
      Some(forgeTag.location()), Some(fTag.location())
    )
  }

  def bothTag(forgeTag: String, fabricTag: String): RecipeIngredientHelper = {
    val forgeKey = ItemTags.create(new ResourceLocation(forgeTag))
    val fabricKey = ItemTags.create(new ResourceLocation(fabricTag))
    RecipeIngredientHelper(
      Ingredient.of(forgeKey), Some(Ingredient.of(fabricKey)),
      Some(forgeKey.location()), Some(fabricKey.location())
    )
  }

  def forgeTagFabricItem(forgeTag: TagKey[Item], fabricItem: ItemLike): RecipeIngredientHelper = {
    RecipeIngredientHelper(
      Ingredient.of(forgeTag), Some(Ingredient.of(fabricItem)),
      Some(forgeTag.location()), None
    )
  }
}
