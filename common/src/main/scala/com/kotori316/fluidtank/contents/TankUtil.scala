package com.kotori316.fluidtank.contents

import net.minecraft.nbt.CompoundTag

object TankUtil {

  def save[A](tank: Tank[A])(implicit access: GenericAccess[A]): CompoundTag = {
    val tag = new CompoundTag()
    tag.putString("type", getType(tank))
    tag.put(access.KEY_CONTENT, access.write(tank.content))
    tag.putByteArray(access.KEY_AMOUNT_GENERIC, tank.capacity.asByteArray)
    tag
  }

  def load[A](tag: CompoundTag)(implicit access: GenericAccess[A]): Tank[A] = {
    val tankType = tag.getString("type")
    val content = access.read(tag.getCompound(access.KEY_CONTENT))
    val capacity = GenericUnit.fromByteArray(tag.getByteArray(access.KEY_AMOUNT_GENERIC))

    tankType match {
      case "Tank" => Tank(content, capacity)
      case "CreativeTank" => new CreativeTank(content, capacity)
      case "VoidTank" => new VoidTank(content, capacity)
      case _ => throw new IllegalArgumentException("Unknown type of tank for %s, %s".formatted(content, tag))
    }
  }

  private def getType(tank: Tank[_]): String = {
    tank match {
      case _: CreativeTank[_] => "CreativeTank"
      case _: VoidTank[_] => "VoidTank"
      case _: Tank[_] => "Tank"
      case _ => throw new IllegalArgumentException("Unknown type of tank, %s".formatted(tank.getClass))
    }
  }
}
