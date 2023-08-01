package com.kotori316.fluidtank.config

import cats.data.{NonEmptyChain, Validated, ValidatedNec}
import cats.implicits.{catsSyntaxOption, catsSyntaxTry, catsSyntaxTuple3Semigroupal}
import cats.{Hash, Show}
import com.google.gson.{GsonBuilder, JsonElement, JsonObject}
import com.kotori316.fluidtank.tank.Tier

import java.nio.file.{Files, Path}
import java.util.Locale
import scala.util.{Failure, Success, Try, Using}

object FluidTankConfig {
  type E = LoadError

  def loadFile(basePath: Path, fileName: String): ValidatedNec[E, ConfigData] = {
    val configPath = basePath.resolve(fileName)
    val json = getFileContent(configPath)
    json.andThen(getConfigDataFromJson)
  }

  def getConfigDataFromJson(j: JsonObject): ValidatedNec[E, ConfigData] = {
    (getCapacity(j), getDouble(j, "renderLowerBound"), getDouble(j, "renderUpperBound"))
      .mapN(ConfigData.apply)
  }

  def createFile(basePath: Path, fileName: String): Unit = {
    val configPath = basePath.resolve(fileName)
    val gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
    val config = ConfigData.DEFAULT
    val json = new JsonObject
    json.addProperty("renderLowerBound", config.renderLowerBound)
    json.addProperty("renderUpperBound", config.renderUpperBound)

    val capacities = new JsonObject
    config.capacityMap.foreach { case (tier, int) =>
      capacities.addProperty(tier.name().toLowerCase(Locale.ROOT), int.toString())
    }
    json.add("capacities", capacities)

    Using(Files.newBufferedWriter(configPath)) { w =>
      gson.toJson(json, w)
    }
  }

  private def getFileContent(filePath: Path): ValidatedNec[E, JsonObject] = {
    if (Files.exists(filePath)) {
      val gson = new GsonBuilder().create()
      val json = Using(Files.newBufferedReader(filePath)) { reader =>
        gson.fromJson(reader, classOf[JsonObject])
      }
      json match {
        case Failure(exception) => Validated.invalidNec(Other("File loading", exception))
        case Success(value) => Validated.validNec(value)
      }
    } else {
      Validated.invalidNec(FileNotFound)
    }
  }

  private def getValue[T](json: JsonObject, key: String, extractor: JsonElement => T, keyPrefix: Seq[String]): ValidatedNec[E, T] = {
    val prefix = if (keyPrefix.isEmpty) "" else keyPrefix.mkString("", ".", ".")
    Validated.valid(json)
      .andThen(j => Option(j.get(key)).toValidNec(KeyNotFound(prefix + key)))
      .andThen(e => Try(extractor(e)).toValidated.leftMap(t => NonEmptyChain.one(Other(prefix + key, t))))
  }

  private def getDouble(json: JsonObject, key: String, keyPrefix: Seq[String] = Nil): ValidatedNec[E, Double] = {
    getValue(json, key, _.getAsDouble, keyPrefix)
  }

  private def getBigInt(json: JsonObject, key: String, keyPrefix: Seq[String] = Nil): ValidatedNec[E, BigInt] = {
    getValue(json, key, e => BigInt(e.getAsString), keyPrefix)
  }

  private def getCapacity(jsonObject: JsonObject): ValidatedNec[E, Map[Tier, BigInt]] = {
    val capacityMap = getValue(jsonObject, "capacities", _.getAsJsonObject, Seq.empty)

    capacityMap.andThen { j =>
      Tier.values().toSeq
        .map(t => getBigInt(j, t.name().toLowerCase(Locale.ROOT), Seq("capacities")).map(b => Seq(t -> b)))
        .reduce((a, b) => a.product(b).map { case (a, b) => a ++ b })
        .map(_.toMap)
    }
  }

  implicit final val hashLoadError: Hash[LoadError] = new Hash[LoadError] {
    override def hash(x: LoadError): Int = x.##

    override def eqv(x: LoadError, y: LoadError): Boolean = {
      (x, y) match {
        case (Other(key1, e1), Other(key2, e2)) => key1 == key2 && e1.getClass == e2.getClass
        case _ => x == y
      }
    }
  }

  implicit final val showLoadError: Show[LoadError] = Show.fromToString

  sealed trait LoadError

  object FileNotFound extends LoadError {
    override def toString: String = "FileNotFound"
  }

  case class KeyNotFound(key: String) extends LoadError

  case class Other(key: String, exception: Throwable) extends LoadError
}
