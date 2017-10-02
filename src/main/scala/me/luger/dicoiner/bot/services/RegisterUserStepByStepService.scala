package me.luger.dicoiner.bot.services
import me.luger.dicoiner.bot.exceptions._
import me.luger.dicoiner.bot.model._
import me.luger.dicoiner.bot.repositories.{FreelancerDAO, RegStatus, RegistrationDAO}
import me.luger.dicoiner.bot.utils.{FreelancerFieldsValidateUtil, TimeZoneParsingUtil}
import org.slf4s.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * @author luger. Created on 28.09.17.
  * @version ${VERSION}
  */
class RegisterUserStepByStepService extends SaveUserService with Logging{
  private val registrationDAO = new RegistrationDAO
  private val freelancerDao = new FreelancerDAO

  def getCurrent(tgId:Long): Future[Option[RegStatus]] =
    registrationDAO.getCurrentRegOperation(tgId)

  //TODO return function
  def getNextStep(tgId:Long): Future[UserRegStatus] =
    registrationDAO.getCurrentRegOperation(tgId).map {
      regStatus =>
        getNextStep(regStatus.getOrElse(RegStatus(registered = false, UserRegStatus.notSaved)))
    }

  def getNextStep(currentState:RegStatus): UserRegStatus = currentState match {
          case RegStatus(_, UserRegStatus.notSaved) => UserRegStatus.bio
          case RegStatus(false, UserRegStatus.bio) => UserRegStatus.phoneNumber
          case RegStatus(false, UserRegStatus.phoneNumber) => UserRegStatus.email
          case RegStatus(false, UserRegStatus.email) => UserRegStatus.workingStack
          case RegStatus(false, UserRegStatus.workingStack) => UserRegStatus.ownStack
          case RegStatus(false, UserRegStatus.ownStack) => UserRegStatus.minRate
          case RegStatus(false, UserRegStatus.minRate) => UserRegStatus.prefferedRate
          case RegStatus(false, UserRegStatus.prefferedRate) => UserRegStatus.timeZone
          case RegStatus(false, UserRegStatus.timeZone) => UserRegStatus.hoursPerWeek
          case RegStatus(false, UserRegStatus.hoursPerWeek) => UserRegStatus.registered
          case RegStatus(true, _) => UserRegStatus.registered
          case _ => UserRegStatus.registered
        }

  import FreelancerFieldsValidateUtil._

  def saveFirst (tgId:Long, tgNick:Option[String]): Future[Long] ={
    for {
      _          <- saveTgInfo(tgId, tgNick)
      regInfo    <- registrationDAO
        .saveRegOperation( tgId, RegStatus(registered = false, UserRegStatus.bio) )
    }yield regInfo
  }

  def validateAndSaveBioReg (tgId: Long, currentStatus:RegStatus, message:String): Future[Try[Option[Freelancer]]] = {
    val bio = message.split(" ").toList
    val name = bio.headOption.getOrElse("")
    val surname = if (bio.isEmpty) "" else bio.tail.headOption.getOrElse("")
    if (name.isEmpty || surname.isEmpty) Failure(new EmptyBioException("имя и фамилию необходимо заполнить"))
    (for {
      bio <- saveBio(tgId, name, surname)
      _   <- registrationDAO.saveRegOperation(tgId, RegStatus (currentStatus.registered, getNextStep(currentStatus)))
    } yield bio).map(x => Success (x))

  }

  def validateAndSavePhoneReg (tgId: Long, currentStatus:RegStatus, message:String): Future[Try[Option[Freelancer]]] ={
    if (!FreelancerFieldsValidateUtil.validatePhoneNumber(message))
      Failure(new InvalidPhoneNumberException("номер телефона не корректен"))
    (for {
      bio <- savePhone(tgId, message)
      _ <- registrationDAO.saveRegOperation(tgId, RegStatus (currentStatus.registered, getNextStep(currentStatus)))
    } yield bio).map(x => Success (x))
  }

  def validateAndSaveEmailReg (tgId: Long, currentStatus:RegStatus, message:String): Future[Try[Option[Freelancer]]] ={
    if (!FreelancerFieldsValidateUtil.validateEmail(message))
      Failure(new InvalidEmailException("email некорректен"))
    (for {
      bio <- saveEmail(tgId, message)
      _ <- registrationDAO.saveRegOperation(tgId, RegStatus (currentStatus.registered, getNextStep(currentStatus)))
    } yield bio).map(x => Success (x))
  }

  def validateAndSaveWorkTechsReg (tgId: Long, currentStatus:RegStatus, message:String): Future[Try[Option[Freelancer]]] ={
    validateAndSave(tgId, currentStatus, message)(saveWorkTechs, validateTechStack,
      _.split("[\\s\\.,;]+").toSeq,
      new InvalidWorkingTechStackException("строка с технологиями некорректна "))
  }

  def validateAndSaveOwnTechsReg (tgId: Long, currentStatus:RegStatus, message:String): Future[Try[Option[Freelancer]]] ={
    validateAndSave(tgId, currentStatus, message)(saveOwnTechs, validateTechStack,
      _.split("[\\s\\.,;]+").toSeq,
      new InvalidWorkingTechStackException("строка с технологиями некорректна "))
  }

  def validateAndSaveMinRateReg (tgId: Long, currentStatus:RegStatus, message:String): Future[Try[Option[Freelancer]]] ={
    validateAndSave(tgId, currentStatus, message)(saveMinRate, validateDoubleRate, _.toDouble,
      new InvalidRateException("значение минимальной ставки не корректно"))
  }

  def validateAndSavePreferredRateReg (tgId: Long, currentStatus:RegStatus, message:String): Future[Try[Option[Freelancer]]] ={
    validateAndSave(tgId, currentStatus, message)(savePreferredRate, validateDoubleRate, _.toDouble,
      new InvalidRateException("значение предпочитаемой ставки не корректно"))
  }

  def validateAndSave[T] (tgId: Long, currentStatus:RegStatus, message:String)(save:(Long, T)=>Future[Option[Freelancer]], validate:String => Boolean, mapTo:(String) => T, failureException:Exception) : Future[Try[Option[Freelancer]]]={
    if (!validate(message)) Future{Failure(failureException)}
    else
      (for {
        data <- save(tgId, mapTo(message))
        _ <- registrationDAO.saveRegOperation(tgId, RegStatus (currentStatus.registered, getNextStep(currentStatus)))
      } yield data).map(x => Success (x))
  }

  def processMessage (tgId: Long, message:String):Future[Try[Option[Freelancer]]] = {
    getCurrent(tgId).flatMap{status =>
      processMessage(tgId,
        status.getOrElse(RegStatus(registered = false, UserRegStatus.notSaved)), message)
    }
  }

  def finishRegistration(tgId: Long): Future[Try[Option[Freelancer]]] = {
    (for {
      _    <- registrationDAO.saveRegOperation(tgId, RegStatus (true, UserRegStatus.registered))
      user <- freelancerDao.getByTgId(tgId)
    } yield user).map(x => Success (x))
  }

  def processMessage(tgId: Long, currentStatus:RegStatus, message:String):Future[Try[Option[Freelancer]]] = {
    currentStatus match {
      case RegStatus(false, UserRegStatus.bio) =>
        validateAndSaveBioReg(tgId, currentStatus, message)
      case RegStatus(_, UserRegStatus.phoneNumber) =>
        validateAndSavePhoneReg(tgId, currentStatus, message)
      case RegStatus(_, UserRegStatus.email) =>
        validateAndSaveEmailReg(tgId, currentStatus, message)
      case RegStatus(_, UserRegStatus.workingStack) =>
        validateAndSaveWorkTechsReg(tgId, currentStatus, message)
      case RegStatus(_, UserRegStatus.ownStack) =>
        validateAndSaveOwnTechsReg(tgId, currentStatus, message)
      case RegStatus(_, UserRegStatus.minRate) =>
        validateAndSaveMinRateReg(tgId, currentStatus, message)
      case RegStatus(_, UserRegStatus.prefferedRate) =>
        validateAndSavePreferredRateReg(tgId, currentStatus, message)
      case RegStatus(_, UserRegStatus.timeZone) =>
        validateAndSave(tgId, currentStatus, message)(
          saveTimeZone, validateTimeZone,
          {x =>  TimeZoneParsingUtil.parseTimeZone(x).right.get},
          new InvalidTimeZoneException("значение часового пояса не корректно"))
      case RegStatus(_, UserRegStatus.hoursPerWeek) =>
        validateAndSave(tgId, currentStatus, message)(saveHours, FreelancerFieldsValidateUtil.validateHoursOfWeek, {x => HoursPerWeek(x).getOrElse(HoursPerWeek.h_5_10)}, new InvalidTimeZoneException("значение количества часов в неделю не корректно"))
      case RegStatus(false, UserRegStatus.registered) =>
        finishRegistration(tgId)
      case _ => freelancerDao.getByTgId(tgId).map(Success(_))
    }
  }
  //TODO rewrite ^ with type classes

  /*trait ValidateAndSave[Status<:UserRegStatus] {
    def validateAndSave (tgId: Long, message:String, status: Status)
  }

  implicit object ValidateAndSaveBio extends ValidateAndSave[UserRegStatus]{

  }*/
}
