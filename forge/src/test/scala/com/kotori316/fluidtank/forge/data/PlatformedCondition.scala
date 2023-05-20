package com.kotori316.fluidtank.forge.data

import com.google.gson.{JsonArray, JsonObject}
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.common.crafting.conditions.{NotCondition, TagEmptyCondition}

import scala.util.chaining.scalaUtilChainingOps

trait PlatformedCondition {
  def forgeCondition: Option[JsonObject]

  def fabricCondition: Option[JsonObject]
}

object PlatformedCondition {

  case class Tag(helper: RecipeIngredientHelper) extends PlatformedCondition {
    override def forgeCondition: Option[JsonObject] = {
      helper.forgeTagLimit.map(t => new NotCondition(new TagEmptyCondition(t)))
        .map(CraftingHelper.serialize)
    }

    override def fabricCondition: Option[JsonObject] = {
      /*
      Create this
      {
        "condition": "fabric:item_tags_populated",
        "values": [
          "c:bronze_ingots"
        ]
      }
      */
      helper.fabricTagLimit.map { t =>
        val o = new JsonObject
        o.addProperty("condition", "fabric:item_tags_populated")
        o.add("values", new JsonArray().tap(_.add(t.toString)))
        o
      }
    }
  }
}
