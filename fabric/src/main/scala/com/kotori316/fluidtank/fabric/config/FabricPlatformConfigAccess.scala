package com.kotori316.fluidtank.fabric.config

import cats.data.Validated
import cats.implicits.catsSyntaxFoldableOps0
import com.kotori316.fluidtank.FluidTankCommon
import com.kotori316.fluidtank.config.{ConfigData, FluidTankConfig, PlatformConfigAccess}
import net.fabricmc.loader.api.FabricLoader

class FabricPlatformConfigAccess extends PlatformConfigAccess {
  private final val configData: ConfigData = {
    val config = FluidTankConfig.loadFile(FabricLoader.getInstance().getConfigDir, "fluidtank-common.json")
    config match {
      case Validated.Valid(a) => cast[ConfigData](a)
      case Validated.Invalid(e) =>
        if (e.contains(FluidTankConfig.FileNotFound)) {
          FluidTankConfig.createFile(FabricLoader.getInstance().getConfigDir, "fluidtank-common.json")
          FluidTankCommon.LOGGER.warn("Created default config file.")
        }
        FluidTankCommon.LOGGER.warn("Get error in loading config, using default value. Errors: {}", e.mkString_(", "))
        ConfigData.DEFAULT
    }
  }

  override def getConfig: ConfigData = configData

  /**
   * Just to ignore syntax error in this class.
   */
  @inline
  private final def cast[A](x: AnyRef): A = {
    x.asInstanceOf[A]
  }
}
