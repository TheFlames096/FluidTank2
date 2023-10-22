package com.kotori316.fluidtank.forge.data

import cats.data.Ior
import com.google.gson.{JsonArray, JsonObject}
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.common.crafting.conditions.{AndCondition, ICondition, NotCondition, TagEmptyCondition}

import scala.util.chaining.scalaUtilChainingOps

trait PlatformedCondition {
  def forgeCondition: Option[ICondition]

  def fabricCondition: Option[JsonObject]
}

object PlatformedCondition {

  case class Tag(helper: RecipeIngredientHelper) extends PlatformedCondition {
    override def forgeCondition: Option[ICondition] = {
      // If fabricTagLimit is empty, the recipe uses vanilla item, so it should be always loaded.
      helper.fabricTagLimit.flatMap { _ =>
        Ior.fromOptions(helper.forgeTagLimit, helper.fabricTagLimit)
          .map { i =>
            i.bimap[ICondition, ICondition](l => new TagEmptyCondition(l), r => new TagEmptyCondition(r))
              .mergeWith((l, r) => new AndCondition(l, r))
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
  }
}
