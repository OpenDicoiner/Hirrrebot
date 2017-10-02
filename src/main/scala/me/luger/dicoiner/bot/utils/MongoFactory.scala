package me.luger.dicoiner.bot.utils

import org.mongodb.scala.{MongoClient, MongoDatabase}

/**
  * @author luger. Created on 28.09.17.
  * @version ${VERSION}
  */
class MongoFactory(customPort:Option[Int]){
  private val SERVER = "localhost"
  private val PORT   = customPort.getOrElse(27017)
  private val DATABASE = "dicoinerbot"
  //TODO mongo init parameters in application config.
  private val mongoClient: MongoClient = MongoClient(s"mongodb://$SERVER:$PORT")
  val database: MongoDatabase = mongoClient.getDatabase(DATABASE)
}
object MongoFactory {
  def apply(customPort:Option[Int] = None): MongoFactory ={
    new MongoFactory(customPort)
  }
}
