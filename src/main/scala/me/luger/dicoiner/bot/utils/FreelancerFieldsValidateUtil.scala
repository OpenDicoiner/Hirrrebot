package me.luger.dicoiner.bot.utils

/**
  * @author luger. Created on 28.09.17.
  * @version ${VERSION}
  */
object FreelancerFieldsValidateUtil {
  private val emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$".r

  /**
    * pattern from SO :)
    */
  private val phoneNumberPattern = "^(?:(?:\\+?1\\s*(?:[.-]\\s*)?)?(?:\\(\\s*([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9])\\s*\\)|([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9]))\\s*(?:[.-]\\s*)?)?([2-9]1[02-9]|[2-9][02-9]1|[2-9][02-9]{2})\\s*(?:[.-]\\s*)?([0-9]{4})(?:\\s*(?:#|x\\.?|ext\\.?|extension)\\s*(\\d+))?$".r

  def validateEmail (email:String) = emailPattern.findFirstIn(email).isDefined

  def validatePhoneNumber (phone:String) = phoneNumberPattern.findFirstIn(phone).isDefined
}
