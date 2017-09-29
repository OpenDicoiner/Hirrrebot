package me.luger.dicoiner.bot.model

/**
  * @author luger. Created on 28.09.17.
  * @version ${VERSION}
  */
trait CaseEnumValue {
  def name:String
  def min:Int
  def max:Int
}

trait CaseHoursEnum {
  type V <: CaseEnumValue
  def values:List[V]
  def unapply(name:String):Option[String] = {
    if (values.exists(_.name == name)) Some(name) else None
  }
  def unapply(value:V):(String, (Int, Int)) = {
    (value.name, (value.min, value.max))
  }
  def apply(name:String):Option[V] = {
    values.find(_.name == name)
  }
  def apply(hval:Int):Option[V] = {
    values.find(x => hval >= x.min  && hval < x.max )
  }
}

abstract class HoursPerWeek (override val name:String, override val min:Int, override val max:Int) extends CaseEnumValue

object HoursPerWeek extends CaseHoursEnum{
  type V = HoursPerWeek
  case object h_5_10 extends HoursPerWeek("5-10", 5, 10)
  case object h_10_20 extends HoursPerWeek("10-20", 10, 20)
  case object h_20_30 extends HoursPerWeek("20-30", 20, 30)
  case object h_30_40 extends HoursPerWeek("30-40", 30, 40)

  val values = List(h_5_10, h_10_20, h_20_30, h_30_40)
}