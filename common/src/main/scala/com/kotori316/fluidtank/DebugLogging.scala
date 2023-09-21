package com.kotori316.fluidtank

import cats.implicits.catsSyntaxEq
import com.google.gson.{GsonBuilder, JsonObject}
import com.kotori316.fluidtank.config.PlatformConfigAccess
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.MinecraftServer
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.config.Configurator

import java.security.SecureClassLoader
import scala.jdk.CollectionConverters.CollectionHasAsScala

object DebugLogging {
  val ENABLED: Boolean = PlatformConfigAccess.getInstance().getConfig.debug

  /**
   * Nullable, check ENABLED before accessing this field.
   */
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
    if (!ENABLED) return
    // Config
    LOGGER.info("Config {}", new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
      .toJson(PlatformConfigAccess.getInstance().getConfig.createJson))
    // Recipes fo FluidTank
    val noPretty = new GsonBuilder().disableHtmlEscaping().create()
    server.getRecipeManager.getRecipes.asScala
      .filter(_.getId.getNamespace === FluidTankCommon.modId)
      .map(r => (r.getId, r.getResultItem(server.registryAccess()), r.getIngredients.asScala.map(_.toJson).zipWithIndex.foldLeft(new JsonObject()) { case (a, (e, i)) => a.add(i.toString, e); a }))
      .map { case (id, stack, value) => s"$id ${BuiltInRegistries.ITEM.getKey(stack.getItem)} x${stack.getCount}(${stack.getTag}) -> ${noPretty.toJson(value)}" }
      .zipWithIndex
      .foreach { case (s, index) => LOGGER.info("{} {}", index + 1, s) }
  }
}
