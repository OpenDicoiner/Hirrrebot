package me.luger.dicoiner.bot.model

/**
  * @author luger. Created on 28.09.17.
  * @version ${VERSION}
  */
trait CommandValue {
  def name:String
}

trait CaseCommandEnum {
  type V <: CommandValue
  def values:List[V]
  def unapply(name:String):Option[String] = {
    if (values.exists(_.name == name)) Some(name) else None
  }
  def unapply(value:V):String = {
    value.name
  }
  def apply(name:String):Option[V] = {
    values.find(_.name == name)
  }

}

abstract class BotCommands (override val name:String) extends CommandValue

object BotCommands extends CaseCommandEnum{
  type V = BotCommands
  case object update extends BotCommands("/update")
  case object help extends BotCommands("/help")
  case object start extends BotCommands("/start")

  val values = List(start, update, help)
}