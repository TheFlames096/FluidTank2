package com.kotori316.fluidtank

import cats.implicits.catsSyntaxEq
import com.google.gson.{GsonBuilder, JsonArray}
import com.kotori316.fluidtank.config.PlatformConfigAccess
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.MinecraftServer
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator

import java.security.SecureClassLoader
import scala.jdk.CollectionConverters.CollectionHasAsScala

object DebugLogging {
  val ENABLED: Boolean = PlatformConfigAccess.getInstance().getConfig.debug

  val LOGGER: Logger = {
    class DummyClassLoader extends SecureClassLoader

    if (ENABLED) {
      val context = Configurator.initialize("fluidtank-config", new DummyClassLoader,
        classOf[FluidTankCommon].getResource("/fluidtank-log4j2.xml").toURI)
      context.getLogger("FluidTankDebug")
    } else {
      null
    }
  }

  def initialLog(server: MinecraftServer): Unit = {
    LOGGER.info("Config {}", new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
      .toJson(PlatformConfigAccess.getInstance().getConfig.createJson))
    val noPretty = new GsonBuilder().disableHtmlEscaping().create()
    server.getRecipeManager.getRecipes.asScala
      .filter(_.getId.getNamespace === FluidTankCommon.modId)
      .map(r => (r.getId, r.getResultItem(server.registryAccess()), r.getIngredients.asScala.map(_.toJson).foldLeft(new JsonArray()) { case (a, e) => a.add(e); a }))
      .map { case (id, stack, value) => s"$id ${BuiltInRegistries.ITEM.getKey(stack.getItem)} x${stack.getCount}(${stack.getTag}) -> ${noPretty.toJson(value)}" }
      .foreach(s => LOGGER.info(s))
  }
}
