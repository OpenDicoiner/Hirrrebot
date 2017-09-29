package me.luger.dicoiner.bot.repositories

import com.mongodb.client.model.Filters
import me.luger.dicoiner.bot.model.{Freelancer, FreelancerDocument, FreelancerFromDoc}
import me.luger.dicoiner.bot.utils.MongoFactory
import org.mongodb.scala.{Completed, Document}
import org.slf4s.Logging

import scala.concurrent.Future

/**
  * @author luger. Created on 28.09.17.
  * @version ${VERSION}
  */
// TODO rewrite with traits and DI
class FreelancerDAO extends Logging{
  private val freelancerCollection = MongoFactory.database.getCollection("freelancers")

  def save (freelancer: Freelancer ): Future[Completed] ={
    freelancerCollection.insertOne(FreelancerDocument(freelancer)).toFuture
  }

  def updateByTgId (freelancer: Freelancer): Future[Document] = {
    freelancerCollection.findOneAndUpdate(Filters.eq("tgId", freelancer.tgInfo.tgId), FreelancerDocument(freelancer)).toFuture
  }

  def getByTgId (id:Long): Future[Option[Freelancer]] = {
    log.info(s"get by id $id")
    freelancerCollection.find(Filters.eq("tgId", id))//TODO rewrite this shit
      .first().map(x => FreelancerFromDoc(id, x)).toFuture.map(x => x.headOption)
  }

  def findAll: Future[Seq[Document]] = {
    freelancerCollection.find.collect().toFuture
  }
}
