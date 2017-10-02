package me.luger.dicoiner.bot.repositories

import com.mongodb.client.model.{Filters, UpdateOptions}
import me.luger.dicoiner.bot.model.UserRegStatus
import me.luger.dicoiner.bot.utils.MongoFactory
import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonBoolean, BsonDocument, BsonInt32, BsonInt64, BsonObjectId}
import org.mongodb.scala.result.UpdateResult
import org.slf4s.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author luger. Created on 28.09.17.
  * @version ${VERSION}
  */
case class RegStatus(registered:Boolean, status:UserRegStatus)

class RegistrationDAO extends Logging{
  private val regStatusCollection = MongoFactory.database.getCollection("regStatus")

  def getCurrentRegOperation (tgUserId:Long): Future[Option[RegStatus]] =
    regStatusCollection
      .find(Filters.eq("tgId", tgUserId))
        .map{x =>
          log.debug(s"current is $x, ${UserRegStatus(x.get("status").map(_.asInt32().getValue).getOrElse(0))}")
          RegStatus(
            x.get("registered").exists(_.asBoolean().getValue),
            {
              val a: Int = x.get("status").map(_.asInt32().getValue).getOrElse(0)
              UserRegStatus(a).getOrElse(UserRegStatus.notSaved)
            })
        }
      .toFuture.map(_.headOption)

  def saveRegOperation(tgUserId:Long, regStatus: RegStatus): Future[Long] = {
    for {
      regStatusDoc: Seq[Document] <- regStatusCollection
        .find(Filters.eq("tgId", tgUserId)).toFuture()
      updatedDoc: UpdateResult <- regStatusDoc match {
        case Nil =>
          val doc:BsonDocument = BsonDocument().append("tgId", BsonInt64(tgUserId))
            .append("registered", BsonBoolean(regStatus.registered))
            .append("status", BsonInt32(regStatus.status.value))
          regStatusCollection.replaceOne(
            Filters.eq("_id", BsonObjectId()), doc, new UpdateOptions().upsert(true)).toFuture
        case h::_ =>
          val _id = h.get("_id").map(_.asObjectId()).getOrElse(BsonObjectId())
          val doc:BsonDocument = BsonDocument().append("tgId", BsonInt64(tgUserId))
              .append("_id", _id)
            .append("registered", BsonBoolean(regStatus.registered))
            .append("status", BsonInt32(regStatus.status.value))
          regStatusCollection.replaceOne(
            Filters.eq("_id", _id), doc, new UpdateOptions().upsert(true)).toFuture
      }
    }yield updatedDoc.getModifiedCount+updatedDoc.getMatchedCount
  }

  def getAll: Future[Seq[Document]] = regStatusCollection.find().toFuture()
}
