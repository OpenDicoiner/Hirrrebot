package me.luger.dicoiner.bot.repositories

import com.mongodb.client.model.Filters
import me.luger.dicoiner.bot.model.{Freelancer, FreelancerDocument, FreelancerFromDoc}
import me.luger.dicoiner.bot.utils.MongoFactory
import org.bson.BsonObjectId
import org.mongodb.scala.Document
import org.mongodb.scala.bson.BsonInt64
import org.slf4s.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author luger. Created on 28.09.17.
  * @version ${VERSION}
  */
// TODO rewrite with traits and DI
class FreelancerDAO extends Logging{
  private val freelancerCollection = MongoFactory.database.getCollection("freelancers")

  def save (freelancer: Freelancer ): Future[Option[Freelancer]] ={
    val a = FreelancerDocument(freelancer)
    for {
      _ <- freelancerCollection.insertOne(a).toFuture
      freelancer <- {
        if (!a.get("_id").isNull && a.get("_id").isObjectId)
          getById(a.get("_id").asObjectId())
        else Future {None}
      }
    }yield freelancer
  }

  def updateByTgId (freelancer: Freelancer): Future[Option[Freelancer]] = {
    freelancerCollection.findOneAndReplace(Filters.eq("tgId", BsonInt64(freelancer.tgInfo.tgId)), FreelancerDocument(freelancer))
      .toFuture()
      .recoverWith { case e: Throwable => log.error("find by ID", e); Future.failed(e)  }
      .map(x => Option(FreelancerFromDoc(x)))
  }

  def getByTgId (id:Long): Future[Option[Freelancer]] = {
    log.info(s"get by id $id")
    freelancerCollection.find(Filters.eq("tgId", BsonInt64(id)))//TODO rewrite this shit
      .toFuture()
      .recoverWith { case e: Throwable => log.error("find by ID", e); Future.failed(e) }
      .map(_.headOption.map(x => FreelancerFromDoc(x)))
  }

  def getById (id:BsonObjectId): Future[Option[Freelancer]] = {
    log.info(s"get by id $id")
    freelancerCollection.find(Filters.eq("_id", id))//TODO rewrite this shit
        .toFuture()
      .recoverWith { case e: Throwable => log.error("find by ID", e); Future.failed(e) }
      .map(_.headOption.map(x => FreelancerFromDoc(x)))
    //.first().map(x => FreelancerFromDoc(x)).toFuture.map(x => x.headOption)
  }

  def findAll: Future[Seq[Document]] = {
    freelancerCollection.find.collect().toFuture
  }
}
