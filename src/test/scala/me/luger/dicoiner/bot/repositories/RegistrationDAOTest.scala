package me.luger.dicoiner.bot.repositories

import me.luger.dicoiner.bot.model.UserRegStatus
import org.scalatest.FunSuite
import org.slf4s.Logging

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * @author luger. Created on 28.09.17.
  * @version ${VERSION}
  */
class RegistrationDAOTest extends FunSuite with Logging{
  val registrationDAO:RegistrationDAO = new RegistrationDAO

  test("testGetCurrentRegOperation") {
    val res = Await.result(registrationDAO.getCurrentRegOperation(123L), Duration.Inf)
    //subscribe((x:Document) => println(x.get("tgNick")))
    log.info(s"$res")
    assert(res.isDefined)
  }

  test("saveRegOperation") {
    val res = Await.result(registrationDAO.saveRegOperation(124L, RegStatus(registered = true, status = UserRegStatus.registered)), Duration.Inf)
    //subscribe((x:Document) => println(x.get("tgNick")))
    log.info(s"$res")
    assert(res>0)
  }

  test("get info about all users") {
    val res = Await.result(registrationDAO.getAll, Duration.Inf)
    //subscribe((x:Document) => println(x.get("tgNick")))
    log.info(s"$res")
    assert(res.nonEmpty)
  }


}
