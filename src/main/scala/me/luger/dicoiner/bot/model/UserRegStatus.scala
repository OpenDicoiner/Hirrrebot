package me.luger.dicoiner.bot.model

/**
  * @author luger. Created on 28.09.17.
  * @version ${VERSION}
  */

trait UserRegEnumValue {
  def name:String
  def message:String
  def value:Int
}

trait CaseRegStatusEnum {
  type V <: UserRegEnumValue
  def values:List[V]
  def unapply(name:String):Option[String] = {
    if (values.exists(_.name == name)) Some(name) else None
  }
  def unapply(value:V):(String, Int) = {
    (value.name, value.value)
  }
  def apply(name:String):Option[V] = {
    values.find(_.name == name)
  }
  def apply(hval:Int):Option[V] = {
    values.find(x => hval == x.value )
  }
}

abstract class UserRegStatus (override val name:String, override val message:String, override val value:Int) extends UserRegEnumValue

object UserRegStatus extends CaseRegStatusEnum{
  type V = UserRegStatus
  case object notSaved extends UserRegStatus("not_saved", "", 0)
  case object registered extends UserRegStatus("registered", "", 11)
  case object bio extends UserRegStatus("bio", "Для начала Ваши имя и фамилия", 1)
  case object phoneNumber extends UserRegStatus("phoneNumber", "Ваш номер телефона", 2)
  case object email extends UserRegStatus("email", "Вашу электронную почту", 3)
  case object workingStack extends UserRegStatus("workingStack",
    "стек технологий, в которых был опыт на реальных проектах (через запятую или пробел)",  4)
  case object ownStack extends UserRegStatus("ownStack",
    """стек технологий,
      |в которых приходилось что-то кодить для себя""".stripMargin, 5)
  case object minRate extends UserRegStatus("minRate",
    """Минимальная ставка для вас\n
      |На случай, если заказчик начнёт демпинговать""".stripMargin,  6)
  case object prefferedRate extends UserRegStatus("prefferedRate",
    "Ставка, за которую Вы готовы работать(в долларах США)", 7)
  case object timeZone extends UserRegStatus("timeZone",
    "Ваш часовой пояс (например GMT+6 или -5:00)", 8)
  case object hoursPerWeek extends UserRegStatus("hoursPerWeek",
    "Количество свободных для работы часов на следующие две недели", 9)
  //case object tgNick extends UserRegStatus("", 10)
  val values = List(notSaved, registered, bio, phoneNumber, email, workingStack, ownStack, minRate, prefferedRate, timeZone, hoursPerWeek)
}
