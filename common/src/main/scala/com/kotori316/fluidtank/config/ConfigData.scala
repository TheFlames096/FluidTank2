package com.kotori316.fluidtank.config

import com.kotori316.fluidtank.tank.Tier

import scala.jdk.javaapi.CollectionConverters

case class ConfigData
(
  capacityMap: Map[Tier, BigInt],
  renderLowerBound: Double,
  renderUpperBound: Double,
  debug: Boolean,
)

object ConfigData {
  final val DEFAULT: ConfigData = ConfigData(
    capacityMap = CollectionConverters.asScala(Tier.getDefaultCapacityMap).toMap,
    renderLowerBound = 0.001d,
    renderUpperBound = 1d - 0.001d,
    debug = false,
  )
}
