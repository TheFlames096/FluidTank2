package com.kotori316.fluidtank.forge.data

import cats.data.Ior
import com.google.gson.{Gson, JsonArray, JsonObject}
import com.mojang.serialization.JsonOps
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraftforge.common.crafting.conditions.{AndCondition, ICondition, NotCondition, TagEmptyCondition}

import scala.jdk.OptionConverters.RichOptional
import scala.util.chaining.scalaUtilChainingOps

trait PlatformedCondition {
  def forgeCondition: Option[ICondition]

  def fabricCondition: Option[JsonObject]

  def neoForgeCondition: Option[JsonObject]
}

// TODO Remove support of different loader tags
object PlatformedCondition {

  case class Tag(helper: RecipeIngredientHelper) extends PlatformedCondition {
    override def forgeCondition: Option[ICondition] = {
      val forgeTagConditionCreator = (t: ResourceLocation) => {
        new TagEmptyCondition(ItemTags.create(t))
      }
      // If fabricTagLimit is empty, the recipe uses vanilla item, so it should be always loaded.
      helper.fabricTagLimit.flatMap { _ =>
        Ior.fromOptions(helper.forgeTagLimit, helper.fabricTagLimit)
          .map { i =>
            i.bimap[ICondition, ICondition](forgeTagConditionCreator, forgeTagConditionCreator)
              .mergeWith((l, r) => new AndCondition(java.util.List.of(l, r)))
          }.map(t => new NotCondition(t))
      }
    }

    override def fabricCondition: Option[JsonObject] = {
      /*
      Create this
      {
        "condition": "fabric:or",
        "values": [
          {
            "condition": "fabric:item_tags_populated",
            "values": [
              "c:bronze_ingots"
            ]
          },
          {
            "condition": "fabric:item_tags_populated",
            "values": [
              "forge:ingots/bronze"
            ]
          }
        ]
      }
      */
      val fabricTagConditionCreator = (t: ResourceLocation) => {
        val o = new JsonObject
        o.addProperty("condition", "fabric:item_tags_populated")
        o.add("values", new JsonArray().tap(_.add(t.toString)))
        o
      }
      helper.fabricTagLimit.flatMap { _ =>
        Ior.fromOptions(helper.fabricTagLimit, helper.forgeTagLimit)
          .map { i =>
            i.bimap(fabricTagConditionCreator, fabricTagConditionCreator)
              .mergeWith { (c1, c2) =>
                val o = new JsonObject
                o.addProperty("condition", "fabric:or")
                o.add("values", new JsonArray().tap(_.add(c1)).tap(_.add(c2)))
                o
              }
          }
      }
    }

    /**
     * Create this
     *
     * {{{
     *   [
     *   {
     *     "type": "neoforge:not",
     *     "value": {
     *       "type": "neoforge:and",
     *       "values": [
     *         {
     *           "type": "neoforge:tag_empty",
     *           "tag": "forge:ingots/copper"
     *         },
     *         {
     *           "type": "neoforge:tag_empty",
     *           "tag": "c:copper_ingots"
     *         }
     *       ]
     *     }
     *   }
     *   ]
     * }}}
     */
    override def neoForgeCondition: Option[JsonObject] = {
      val gson = new Gson()
      forgeCondition.flatMap(c => ICondition.CODEC.encodeStart(JsonOps.INSTANCE, c).result().toScala)
        .map(j => gson.toJson(j))
        .map(s => s
          .replace("forge:not", "neoforge:not")
          .replace("forge:and", "neoforge:and")
          .replace("forge:tag_empty", "neoforge:tag_empty")
        )
        .map(s => gson.fromJson(s, classOf[JsonObject]))
    }
  }
}
