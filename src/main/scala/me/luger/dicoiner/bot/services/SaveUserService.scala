package me.luger.dicoiner.bot.services

import java.util.TimeZone

import me.luger.dicoiner.bot.model._
import me.luger.dicoiner.bot.repositories.FreelancerDAO

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author luger. Created on 30.09.17.
  * @version ${VERSION}
  */
trait SaveUserService {
  private val freelancerDAO = new FreelancerDAO

  def saveBio (tgId:Long, name:String, surname:String): Future[Option[Freelancer]] = for {
    freelancer <- freelancerDAO.getByTgId(tgId)
    saved <- freelancer match {
      case None => Future {None}
      case Some(x) =>
        freelancerDAO.updateByTgId(x.copy(bio = Bio( name = Option(name), surname = Option(surname) )))

    }
  }yield saved

  def saveTgInfo (tgId:Long, chatId:Long, tgName:Option[String]): Future[Option[Freelancer]] = for {
    freelancer <- freelancerDAO.getByTgId(tgId)
    saved <- freelancer match {
      case None =>
        freelancerDAO.save(Freelancer(None,tgInfo = TgInfo(tgId = tgId, chatId = chatId, tgNick = tgName)))
      case Some(x) =>
        freelancerDAO.updateByTgId(x.copy( tgInfo = TgInfo (tgName, tgId, chatId) ))

    }
  }yield saved

  def savePhone (tgId:Long, phoneNumber : String ): Future[Option[Freelancer]] = for {
    freelancer <- freelancerDAO.getByTgId(tgId)
    saved <- freelancer match {
      case None => Future {None}
      case Some(x) =>
        freelancerDAO.updateByTgId(x.copy( phoneNumber = Option(phoneNumber) ))

    }
  }yield saved

  def saveEmail (tgId:Long, email : String ): Future[Option[Freelancer]] =
    for {
      freelancer <- freelancerDAO.getByTgId(tgId)
      saved <- freelancer match {
        case None => Future {None}
        case Some(x) =>
          freelancerDAO.updateByTgId(x.copy( email = Option(email) ))
      }
    }yield saved

  def saveWorkTechs (tgId:Long, workingTechs : Seq[String] ): Future[Option[Freelancer]] =
    for {
      freelancer <- freelancerDAO.getByTgId(tgId)
      saved <- freelancer match {
        case None => Future {None}
        case Some(x) =>
          freelancerDAO.updateByTgId(x.copy( workingTechStack = TechStack( workingTechs ) ))
      }
    }yield saved

  def saveOwnTechs (tgId:Long, ownTechs : Seq[String] ): Future[Option[Freelancer]] =
    for {
      freelancer <- freelancerDAO.getByTgId(tgId)
      saved <- freelancer match {
        case None => Future {None}
        case Some(x) =>
          freelancerDAO.updateByTgId(x.copy( ownTechStack = TechStack( ownTechs ) ))
      }
    }yield saved

  def saveMinRate (tgId:Long, minRate:Double ): Future[Option[Freelancer]] =
    for {
      freelancer <- freelancerDAO.getByTgId(tgId)
      saved <- freelancer match {
        case None => Future {None}
        case Some(x) =>
          freelancerDAO.updateByTgId(x.copy( minRate = Option(minRate) ))
      }
    }yield saved

  def savePreferredRate (tgId:Long, prefferedRate:Double ): Future[Option[Freelancer]] =
    for {
      freelancer <- freelancerDAO.getByTgId(tgId)
      saved <- freelancer match {
        case None => Future {None}
        case Some(x) =>
          freelancerDAO.updateByTgId(x.copy( prefferedRate = Option(prefferedRate) ))
      }
    }yield saved

  def saveTimeZone (tgId:Long, timeZone:TimeZone ): Future[Option[Freelancer]] =
    for {
      freelancer <- freelancerDAO.getByTgId(tgId)
      saved <- freelancer match {
        case None => Future {None}
        case Some(x) =>
          freelancerDAO.updateByTgId(x.copy( timeZone = Option(timeZone) ))
      }
    }yield saved

  def saveHours (tgId:Long, hoursPerWeek:HoursPerWeek ): Future[Option[Freelancer]] =
    for {
      freelancer <- freelancerDAO.getByTgId(tgId)
      saved <- freelancer match {
        case None => Future {None}
        case Some(x) =>
          freelancerDAO.updateByTgId(x.copy( hoursPerWeek = Option(hoursPerWeek) ))
      }
    }yield saved

}
