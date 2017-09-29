package me.luger.dicoiner.bot.repositories

import com.mongodb.client.model.Filters
import me.luger.dicoiner.bot.utils.MongoFactory
import org.slf4s.Logging
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author luger. Created on 28.09.17.
  * @version ${VERSION}
  */
class RegistrationDAO extends Logging{
  private val regStatusCollection = MongoFactory.database.getCollection("regStatus")

  def getCurrentRegOperation (tgUserId:Long): Future[Option[Int]] =
    regStatusCollection
      .find(Filters.eq("tgId", tgUserId))
        .map(x => x.get("status").map(_.asInt32().getValue).getOrElse(0))
      .toFuture.map(_.headOption)


}
