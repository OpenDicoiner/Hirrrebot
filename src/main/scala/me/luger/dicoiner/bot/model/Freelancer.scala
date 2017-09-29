package me.luger.dicoiner.bot.model

import java.util.TimeZone

import me.luger.dicoiner.bot.utils.TimeZoneParsingUtil
import org.bson.BsonValue
import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonObjectId}

/**
  * @author luger. Created on 28.09.17.
  * @version ${VERSION}
  */
case class Bio (surname:Option[String], name:Option[String])
case class TgInfo (tgNick:Option[String], tgId:Long)
case class TechStack(techs:Seq[String])
case class Freelancer(
                      _id : BsonObjectId,
                      bio:Bio,
                      phoneNumber:Option[String],
                      email:Option[String],
                      tgInfo: TgInfo,
                      workingTechStack: TechStack,
                      ownTechStack:TechStack,
                      minRate:Option[Double],
                      prefferedRate:Option[Double],
                      timeZone: Option[TimeZone],
                      hoursPerWeek: Option[HoursPerWeek])

object FreelancerDocument {
  def apply(freelancer: Freelancer): BsonDocument = {
    BsonDocument(
      "_id" -> freelancer._id,
      "name" -> freelancer.bio.name.getOrElse(""),
      "surname" -> freelancer.bio.surname.getOrElse(""),
      "phoneNumber" -> freelancer.phoneNumber.getOrElse(""),
      "tgId" -> freelancer.tgInfo.tgId,
      "tgNick" -> freelancer.tgInfo.tgNick.getOrElse(""),
      "workingTechStack" -> BsonArray(freelancer.workingTechStack.techs),
      "ownTechStack" -> BsonArray(freelancer.ownTechStack.techs),
      "minRate" -> freelancer.minRate.getOrElse(0.0),
      "prefferedRate" -> freelancer.prefferedRate.getOrElse(0.0),
      "timeZone" -> freelancer.timeZone.map(_.getDisplayName).getOrElse(""),
      "hoursPerWeek" -> freelancer.hoursPerWeek.map(_.name).getOrElse("")
    )
  }

}

object FreelancerFromDoc{
  def apply(tgId:Long, freelancer: Document): Freelancer = {
    Freelancer(
      _id = freelancer.get("_id").map(_.asDocument()).head.asObjectId(),
      bio = Bio(
        name = freelancer.get("name").map(x => x).bsonToString(),
        surname = freelancer.get("surname").bsonToString()
      ),
      phoneNumber = freelancer.get("phoneNumber").bsonToString(),
      email = freelancer.get("email").bsonToString(),
      tgInfo = TgInfo(
        tgId = tgId,
        tgNick = freelancer.get("tgNick").bsonToString()
      ),
      workingTechStack = TechStack(
        freelancer.get("workingTechStack").bsonArrayToSeq()
      ),
      ownTechStack = TechStack(
        freelancer.get("ownTechStack").bsonArrayToSeq()
      ),
      minRate = freelancer.get("minRate").bsonToDouble(),
      prefferedRate = freelancer.get("prefferedRate").bsonToDouble(),
      timeZone = freelancer.get("timeZone").bsonToTimeZone(),
      hoursPerWeek = freelancer.get("hoursPerWeek")
        .map(x => HoursPerWeek(x.asString().getValue)).get
    )
  }

  implicit class BsonToStringOps(b:Option[BsonValue]) {
    def bsonToString(): Option[String] = b.map(_.asString().getValue)

    def bsonToDouble(): Option[Double] = b.map(_.asDouble().getValue)

    def bsonToTimeZone(): Option[TimeZone] = {
      val s = b.map(x => x.asString().getValue)
      TimeZoneParsingUtil.parseTimeZone(s.getOrElse("")) match {
        case Left (_) => None
        case Right (x) => Option(x)
      }
    }

    def bsonArrayToSeq ():Seq[String] = {
      import scala.collection.JavaConverters._
      b.map(_.asArray().getValues.asScala.map(_.asString().getValue).toSeq).getOrElse(Seq.empty)
    }
  }
}

