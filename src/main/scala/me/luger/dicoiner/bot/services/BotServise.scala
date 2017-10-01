package me.luger.dicoiner.bot.services

import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{ChatActions, Polling, TelegramBot}
import info.mukel.telegrambot4s.models.Message
import me.luger.dicoiner.bot.model.{Freelancer, TgInfo, UserRegStatus}
import me.luger.dicoiner.bot.repositories.{FreelancerDAO, RegStatus, RegistrationDAO}
import org.bson.BsonObjectId
import org.mongodb.scala.bson.BsonObjectId
import org.slf4s.Logging

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * @author luger. Created on 27.09.17.
  * @version ${VERSION}
  */
class BotServise(val token:String)
  extends TelegramBot
    with Polling
    with Commands
    with Callbacks
    with Logging{
  val registerUserStepByStepService = new RegisterUserStepByStepService
  val registrationDAO = new RegistrationDAO

  onCommand("/start"){implicit message =>
    message.from match {
      case None => reply("Извините, какая-то ошибка")
      case Some(u) =>
        val tgId = u.id
        val tgNick = u.username
        registerUserStepByStepService.getCurrent(tgId).map {
          case None | Some( RegStatus(_, UserRegStatus.notSaved) ) =>
            registerUserStepByStepService.saveFirst(tgId, tgNick).onComplete{
              case Success(_)=>
                reply("""Приветствую Вас.
                        |Для регистрации Вам необходимо заполнить простую анкету.
                        |Для начала Ваши имя и фамилия:""".stripMargin)
              case Failure(ex)=>
                log.error("oops", ex)
            }
          case Some (regStatus) =>
            message.text match {
              case None       => reply (regStatus.status.message)
              case Some(text) =>
                registerUserStepByStepService.processMessage(tgId, regStatus, text) map {
                  case Failure (ex)=>
                    reply(ex.getMessage)
                    reply(regStatus.status.message)
                  case Success (x)=> registerUserStepByStepService.getNextStep(tgId) map {x =>
                    reply(x.message)
                  }
                }
            }
        }
    }
  }

  // TODO
  onCommand("/help"){ implicit message =>
    reply("")
  }

  override def receiveMessage(message: Message): Unit = {
    log.info (s"receiveMessage: ${message.from}")
    implicit val msg = message
    message.from match {
      case None => reply("Извините, какая-то ошибка")
      case Some(u) =>
        val tgId = u.id
        val tgNick = u.username
        registerUserStepByStepService.getCurrent(tgId).map {
          case None | Some( RegStatus(_, UserRegStatus.notSaved) ) =>
            registerUserStepByStepService.saveFirst(tgId, tgNick).onComplete{
              case Success(_)=>
                reply("""Приветствую Вас.
                        |Для регистрации Вам необходимо заполнить простую анкету.
                        |Для начала Ваши имя и фамилия:""".stripMargin)
              case Failure(ex)=>
                log.error("oops", ex)
            }
          case Some (regStatus) =>
            message.text match {
              case None       => reply (regStatus.status.message)
              case Some(text) =>
                registerUserStepByStepService.processMessage(tgId, regStatus, text) map {
                  case Failure (ex)=>
                    reply(ex.getMessage)
                    reply(regStatus.status.message)
                  case Success (x)=> registerUserStepByStepService.getNextStep(tgId) map {x =>
                    reply(x.message)
                  }
                }
            }
        }
    }
  }
}