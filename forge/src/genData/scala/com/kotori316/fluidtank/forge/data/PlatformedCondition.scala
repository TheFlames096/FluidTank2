package com.kotori316.fluidtank.forge.data

import com.google.gson.{JsonArray, JsonObject}
import net.minecraft.tags.ItemTags
import net.minecraftforge.common.crafting.conditions.{ICondition, NotCondition, TagEmptyCondition}

import scala.util.chaining.scalaUtilChainingOps

trait PlatformedCondition {
  def forgeCondition: Option[ICondition]

  def fabricCondition: Option[JsonObject]
}

object PlatformedCondition {

  case class Tag(helper: RecipeIngredientHelper) extends PlatformedCondition {
    override def forgeCondition: Option[ICondition] = {
      helper.forgeTagLimit.map(t => new NotCondition(new TagEmptyCondition(ItemTags.create(t))))
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
