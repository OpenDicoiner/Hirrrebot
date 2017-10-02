package me.luger.dicoiner.bot.repositories

import java.time.ZoneId
import java.util.TimeZone

import com.dimafeng.testcontainers.{ForAllTestContainer, GenericContainer}
import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
import de.flapdoodle.embed.mongo.distribution.Version
import me.luger.dicoiner.bot.model._
import me.luger.dicoiner.bot.utils.MongoFactory
import org.mongodb.scala.bson.BsonObjectId
import org.scalatest.{BeforeAndAfter, FlatSpec, FunSuite}
import org.slf4s.Logging
import org.testcontainers.containers.wait.Wait

import scala.concurrent.Await
import scala.concurrent.duration.Duration
/**
  * @author luger. Created on 28.09.17.
  * @version ${VERSION}
  */
class RegistrationDAOTest extends FlatSpec with Logging  with ForAllTestContainer {

  override val container = GenericContainer("mongo:latest",
    exposedPorts = Seq(27017),
    waitStrategy = Wait.forListeningPort()
  )
  lazy val freelancerDAO: FreelancerDAO = new FreelancerDAO(Some(container.container.getMappedPort(27017)))
  lazy val registrationDAO:RegistrationDAO = new RegistrationDAO(Some(container.container.getMappedPort(27017)))
/*  before {
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
  }*/

  it should "saveRegOperation" in{
      val res = Await.result(registrationDAO.saveRegOperation(123L, RegStatus(registered = true, status = UserRegStatus.registered)), Duration.Inf)
      log.info(s"$res")
      //assert(res.isObjectId)
      //assert(res.asObjectId().getValue.toHexString.nonEmpty)
  }

  it should "testGetCurrentRegOperation" in {
      Await.result(registrationDAO.saveRegOperation(123L, RegStatus(registered = true, status = UserRegStatus.registered)), Duration.Inf)
      val res = Await.result(registrationDAO.getCurrentRegOperation(123L), Duration.Inf)
      //subscribe((x:Document) => println(x.get("tgNick")))
      log.info(s"$res")
      assert(res.isDefined)
  }

  it should "get info about all users" in {
      val res = Await.result(registrationDAO.getAll, Duration.Inf)
      //subscribe((x:Document) => println(x.get("tgNick")))
      log.info(s"$res")
      assert(res.nonEmpty)
  }

}
