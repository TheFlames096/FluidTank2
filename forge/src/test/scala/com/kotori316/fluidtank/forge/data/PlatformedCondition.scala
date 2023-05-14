package com.kotori316.fluidtank.forge.data

import com.google.gson.{JsonArray, JsonObject}
import net.minecraft.tags.TagKey
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.common.crafting.conditions.{NotCondition, TagEmptyCondition}

import scala.util.chaining.scalaUtilChainingOps

trait PlatformedCondition {
  def forgeCondition: JsonObject

  def fabricCondition: JsonObject
}

object PlatformedCondition {

  case class Tag(forgeTag: TagKey[_], fabricTag: TagKey[_]) extends PlatformedCondition {
    override def forgeCondition: JsonObject = {
      val condition = new NotCondition(new TagEmptyCondition(forgeTag.location()))
      CraftingHelper.serialize(condition)
    }

    override def fabricCondition: JsonObject = {
      /*
      Create this
      {
        "condition": "fabric:item_tags_populated",
        "values": [
          "c:bronze_ingots"
        ]
      }
      */
      val o = new JsonObject
      o.addProperty("condition", "fabric:item_tags_populated")
      o.add("values", new JsonArray().tap(_.add(fabricTag.location().toString)))
      o
    }
  }
}
