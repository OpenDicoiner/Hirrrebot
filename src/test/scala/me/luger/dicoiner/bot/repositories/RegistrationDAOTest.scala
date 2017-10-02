package me.luger.dicoiner.bot.repositories

import java.time.ZoneId
import java.util.TimeZone

import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
import de.flapdoodle.embed.mongo.distribution.Version
import me.luger.dicoiner.bot.model._
import me.luger.dicoiner.bot.utils.MongoFactory
import org.mongodb.scala.bson.BsonObjectId
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.slf4s.Logging

import scala.concurrent.Await
import scala.concurrent.duration.Duration
/**
  * @author luger. Created on 28.09.17.
  * @version ${VERSION}
  */
class RegistrationDAOTest extends FunSuite with BeforeAndAfter with Logging with MongoEmbedDatabase {
  val registrationDAO:RegistrationDAO = new RegistrationDAO
  val freelancerDAO: FreelancerDAO = new FreelancerDAO

  before {
    withEmbedMongoFixture(27017, Version.V3_5_1) { mongodProps =>
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
      Await.result(registrationDAO.saveRegOperation(123L, RegStatus(registered = true, status = UserRegStatus.registered)), Duration.Inf)
    }
  }

  test("saveRegOperation") {
    withEmbedMongoFixture(27017, Version.V3_5_1) { mongodProps =>
      val res = Await.result(registrationDAO.saveRegOperation(123L, RegStatus(registered = true, status = UserRegStatus.registered)), Duration.Inf)
      log.info(s"$res")
      //assert(res.isObjectId)
      //assert(res.asObjectId().getValue.toHexString.nonEmpty)
    }
  }

  test("testGetCurrentRegOperation") {
    withEmbedMongoFixture(27017, Version.V3_5_1) { mongodProps =>
      Await.result(registrationDAO.saveRegOperation(123L, RegStatus(registered = true, status = UserRegStatus.registered)), Duration.Inf)
      val res = Await.result(registrationDAO.getCurrentRegOperation(123L), Duration.Inf)
      //subscribe((x:Document) => println(x.get("tgNick")))
      log.info(s"$res")
      assert(res.isDefined)
    }
  }

  test("get info about all users") {
    withEmbedMongoFixture(27017, Version.V3_5_1) { mongodProps =>
      val res = Await.result(registrationDAO.getAll, Duration.Inf)
      //subscribe((x:Document) => println(x.get("tgNick")))
      log.info(s"$res")
      assert(res.nonEmpty)
    }
  }

  after{
    Await.result(MongoFactory.database.getCollection("freelancers").drop().toFuture, Duration.Inf)
    Await.result(MongoFactory.database.getCollection("regStatus").drop().toFuture, Duration.Inf)
  }
}
