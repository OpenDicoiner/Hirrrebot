package me.luger.dicoiner.bot.repositories

import java.time.ZoneId
import java.util.TimeZone

import com.mongodb.client.model.Filters
import me.luger.dicoiner.bot.model._
import me.luger.dicoiner.bot.utils.MongoFactory
import org.mongodb.scala.Completed
import org.mongodb.scala.bson.BsonObjectId
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.slf4s.Logging

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, MINUTES}

/**
  * @author luger. Created on 28.09.17.
  * @version ${VERSION}
  */
class FreelancerDAOTest extends FunSuite with BeforeAndAfter with Logging {
  val freelancerDAO: FreelancerDAO = new FreelancerDAO

  before{
    createFreelancer
  }

  def createFreelancer = {
    Await.result(freelancerDAO.save(
      Freelancer(
        _id = Option(BsonObjectId()),
        bio = Bio(name = Option ("Luger"), surname = Option ("Parabellum")),
        email = Option("ads@asd"),
        hoursPerWeek = Option(HoursPerWeek.h_5_10),
        minRate = Option(20.0),
        prefferedRate = Option(20.0),
        ownTechStack = TechStack(Seq("sdf", "sdf", "sdf")),
        workingTechStack = TechStack(Seq("sdf", "sdf", "sdf")),
        phoneNumber = Option("123123"),
        tgInfo = TgInfo(tgId = 123L, tgNick = Option("ewrerwer"), chatId = 123),
        timeZone = Option(TimeZone.getTimeZone(ZoneId.of("GMT+6")))
      )), Duration.Inf)
  }

  test("testSave") {
    val res = Await.result(freelancerDAO.save(
      Freelancer(
      _id = Option(BsonObjectId()),
      bio = Bio(name = Option ("Luger"), surname = Option ("Parabellum")),
      email = Option("ads@asd"),
      hoursPerWeek = Option(HoursPerWeek.h_5_10),
      minRate = Option(20.0),
      prefferedRate = Option(20.0),
      ownTechStack = TechStack(Seq("sdf", "sdf", "sdf")),
      workingTechStack = TechStack(Seq("sdf", "sdf", "sdf")),
      phoneNumber = Option("123123"),
      tgInfo = TgInfo(tgId = 124L, tgNick = Option("ewrerwer"), chatId = 123),
      timeZone = Option(TimeZone.getTimeZone(ZoneId.of("GMT+6")))
    )), Duration.Inf)
      assert(res.isDefined)
      assert(res.get._id.isDefined)
  }

  test("test get by telegram id"){
    createFreelancer
    val res: Option[Freelancer] = Await.result(freelancerDAO.getByTgId(123L), Duration.Inf)
    log.info(s"res : $res")
    assert (res.isDefined)
    assert(res.get.tgInfo.tgNick.isDefined)
    assert(res.get.tgInfo.tgNick.get === "ewrerwer")

  }

  test("find all"){
    val res = Await.result(freelancerDAO.findAll, Duration.Inf)
    //subscribe((x:Document) => println(x.get("tgNick")))
    log.info(s"$res")
    assert(res.nonEmpty)
  }

  after{
    Await.result(MongoFactory.database.getCollection("freelancers").drop().toFuture, Duration.Inf)
  }
}
