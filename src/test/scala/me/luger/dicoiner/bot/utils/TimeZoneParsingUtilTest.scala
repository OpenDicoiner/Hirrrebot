package me.luger.dicoiner.bot.utils

import java.time.ZoneId
import java.util.TimeZone

import org.scalatest.FunSuite

/**
  * @author luger. Created on 28.09.17.
  * @version ${VERSION}
  */
class TimeZoneParsingUtilTest extends FunSuite {
  import TimeZoneParsingUtil._

  test("testParseTimeZone:test for GMT+6") {
    parseTimeZone("GMT+6") match {
      case Right(x) => assert (x === TimeZone.getTimeZone(ZoneId.of("GMT+6")))
      case Left(ex) => throw ex
    }
  }

}
