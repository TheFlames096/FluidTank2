package com.kotori316.fluidtank.forge.data

import com.google.gson.JsonArray
import com.kotori316.fluidtank.FluidTankCommon
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import org.slf4j.MarkerFactory

import java.util.Collections
import scala.jdk.javaapi.CollectionConverters

@Mod.EventBusSubscriber(modid = FluidTankCommon.modId, bus = Mod.EventBusSubscriber.Bus.MOD)
object FluidTankDataProvider {
  final val MARKER = MarkerFactory.getMarker("FluidTankDataProvider")

  @SubscribeEvent
  def gatherDataEvent(event: GatherDataEvent): Unit = {
    FluidTankCommon.LOGGER.info(MARKER, "Start data generation")
    // Loot table
    event.getGenerator.addProvider(event.includeServer(), new LootTableProvider(event.getGenerator.getPackOutput, Collections.emptySet(),
      CollectionConverters.asJava(Seq(new LootTableProvider.SubProviderEntry(() => new LootSubProvider, LootContextParamSets.BLOCK)))
    ))
    // State and model
    event.getGenerator.addProvider(event.includeClient(), new StateAndModelProvider(event.getGenerator, event.getExistingFileHelper))
    // Recipe
    event.getGenerator.addProvider(event.includeServer(), new RecipeProvider(event.getGenerator))
  }

  def makeForgeConditionArray(conditions: List[PlatformedCondition]): JsonArray = {
    conditions.flatMap(_.forgeCondition).foldLeft(new JsonArray) { case (a, c) => a.add(c); a }
  }

  def makeFabricConditionArray(conditions: List[PlatformedCondition]): JsonArray = {
    conditions.flatMap(_.fabricCondition).foldLeft(new JsonArray) { case (a, c) => a.add(c); a }
  }
}
