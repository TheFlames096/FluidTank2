package com.kotori316.fluidtank.forge.data

import com.google.gson.{JsonArray, JsonObject}
import com.mojang.serialization.JsonOps
import net.minecraft.tags.ItemTags
import net.minecraftforge.common.crafting.conditions.{ICondition, NotCondition, TagEmptyCondition}

import scala.jdk.OptionConverters.RichOptional
import scala.util.chaining.scalaUtilChainingOps

trait PlatformedCondition {
  def forgeCondition: Option[JsonObject]

  def fabricCondition: Option[JsonObject]
}

object PlatformedCondition {

  case class Tag(helper: RecipeIngredientHelper) extends PlatformedCondition {
    override def forgeCondition: Option[JsonObject] = {
      helper.forgeTagLimit.map(t => new NotCondition(new TagEmptyCondition(ItemTags.create(t))))
        .flatMap(c => ICondition.CODEC.encodeStart(JsonOps.INSTANCE, c).result().toScala)
        .collect{ case jO: JsonObject => jO}
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
