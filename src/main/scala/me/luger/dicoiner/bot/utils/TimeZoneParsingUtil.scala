package me.luger.dicoiner.bot.utils

import java.time.ZoneId
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.util.TimeZone

import org.slf4s.Logging

/**
  * @author luger. Created on 28.09.17.
  * @version ${VERSION}
  */
object TimeZoneParsingUtil extends Logging{
  private[this] val localizedZoneOffsetFormatter = DateTimeFormatter.ofPattern("O")
  private[this] val zoneIdFormatter = DateTimeFormatter.ofPattern("VV")

  private def parseWithOffset (timeZoneStr:String): Either[DateTimeParseException, TimeZone] ={
    try {
      val temporal = localizedZoneOffsetFormatter.parse(timeZoneStr)
      Right (TimeZone.getTimeZone(ZoneId.from(temporal)))
    }catch{
      case ex:DateTimeParseException =>
        log.error(s"timezone not recognized")
        Left(ex)
    }
  }

  private def parseWithZoneId (timeZoneStr:String): Either[DateTimeParseException, TimeZone] ={
    try {
      val temporal = zoneIdFormatter.parse(timeZoneStr)
      Right (TimeZone.getTimeZone(ZoneId.from(temporal)))
    }catch{
      case ex:DateTimeParseException => Left(ex)
    }
  }

  def parseTimeZone (timeZoneStr:String): Either[DateTimeParseException, TimeZone] = {
    parseWithOffset(timeZoneStr).leftOrElse(parseWithZoneId(timeZoneStr))
  }

  implicit class EitherOps[A, B](e1: Either[A, B]) {
    def leftOrElse(e2: => Either[A, B]): Either[A,B] =
      e1 match {
        case Left(_) => e2
        case Right(b) => Right(b)
      }
  }
}
