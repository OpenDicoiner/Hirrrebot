package me.luger.dicoiner.bot.utils

import org.slf4s.Logging

/**
  * @author luger. Created on 28.09.17.
  * @version ${VERSION}
  */
object FreelancerFieldsValidateUtil extends Logging{
  private val emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$".r

  /**
    * pattern from SO :)
    */
  private val phoneNumberPattern = "^(?:(?:\\+?1\\s*(?:[.-]\\s*)?)?(?:\\(\\s*([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9])\\s*\\)|([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9]))\\s*(?:[.-]\\s*)?)?([2-9]1[02-9]|[2-9][02-9]1|[2-9][02-9]{2})\\s*(?:[.-]\\s*)?([0-9]{4})(?:\\s*(?:#|x\\.?|ext\\.?|extension)\\s*(\\d+))?$".r

  def validateEmail (email:String) = emailPattern.findFirstIn(email).isDefined

  def validatePhoneNumber (phone:String) = phoneNumberPattern.findFirstIn(phone).isDefined

  def validateTechStack (message:String) = message.split("[\\s\\.,;]+").nonEmpty

  def validateDoubleRate (message:String) = try{
    message.toDouble
    true
  }catch {
    case ex:NumberFormatException =>
      log.error("validating double rate was broken", ex)
      false
  }

  def validateTimeZone (message: String ):Boolean = TimeZoneParsingUtil.parseTimeZone(message) match {
    case Left (_) => false
    case Right (_)=> true
  }

  def validateHoursOfWeek (message: String ):Boolean =
    "\\d+-\\d+".r.findFirstIn(message).isDefined
}
