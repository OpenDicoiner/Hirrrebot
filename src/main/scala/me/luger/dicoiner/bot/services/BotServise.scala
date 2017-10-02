package me.luger.dicoiner.bot.services

import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.models.{InlineKeyboardButton, InlineKeyboardMarkup, Message}
import me.luger.dicoiner.bot.model.{BotCommands, HoursPerWeek, UserRegStatus}
import me.luger.dicoiner.bot.repositories.{RegStatus, RegistrationDAO}
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
  val HOURS_TAG = "HOURS_PER_WEEK"
  val UPDATE_TAG = "UPDATE_INFO"

  private def hoursTag = prefixTag(HOURS_TAG)_
  private def updTag = prefixTag(UPDATE_TAG)_

  val hoursButtons: InlineKeyboardMarkup = InlineKeyboardMarkup.singleRow(HoursPerWeek.values.map{ x=>InlineKeyboardButton.callbackData(x.name, hoursTag (x.name))})

  val updateButtons: InlineKeyboardMarkup = InlineKeyboardMarkup.singleColumn(
    UserRegStatus.values
      .filter{x=> x != UserRegStatus.notSaved &&
        x != UserRegStatus.registered &&
        x != UserRegStatus.bio}
      .map{x=>InlineKeyboardButton.callbackData(x.updateMessage, updTag (x.name))}
  )

  onCallbackWithTag(HOURS_TAG){implicit cbq =>
    ackCallback()
    val u = cbq.from
    val data = cbq.data
    cbq.message.map{implicit msg =>
      for {
        regStatus <- registerUserStepByStepService.getCurrent(u.id)
        dd = regStatus.getOrElse(RegStatus(false, UserRegStatus.notSaved))
        _         <-
        registerUserStepByStepService.processMessage(u.id.toLong, dd, data.getOrElse("")) map {
          case Failure (ex)=>
            reply(s"${ex.getMessage}\n${dd.status.message}")
          case Success (_)=> Future.successful(registerUserStepByStepService.getNextStep(dd)) map {x =>
            reply(x.message)
          }
        }
      }yield dd
    }
  }

  onCallbackWithTag(UPDATE_TAG){implicit cbq =>
    ackCallback()
    val u = cbq.from
    val data: Option[UserRegStatus] = UserRegStatus(cbq.data.getOrElse("registered"))

    cbq.message.map { implicit msg =>
      registerUserStepByStepService.getCurrent(u.id).flatMap{x=>
        val dd = x.getOrElse(RegStatus(false, UserRegStatus.notSaved))
        val status = data.getOrElse(UserRegStatus.notSaved)
        reply(status.message)
        registrationDAO.saveRegOperation(u.id, RegStatus(dd.registered, status))
      }
    }
  }

  onMessage{implicit message =>
    if (!BotCommands.values.map(_.name).contains(message.text.getOrElse("").split(" ").head)){
      message.from match {
        case None => reply("Извините, какая-то ошибка")
        case Some(u) =>
          val tgId = u.id
          val tgNick = u.username
          registerUserStepByStepService.getCurrent(tgId).map {
            case None | Some( RegStatus(_, UserRegStatus.notSaved) ) =>
              log.info("save first")
              registerUserStepByStepService.saveFirst(tgId, tgNick).onComplete{
                case Success(_)=>
                  reply("""Приветствую Вас.
                          |Для регистрации Вам необходимо заполнить простую анкету.
                          |Для начала Ваши имя и фамилия:""".stripMargin)
                case Failure(ex)=>
                  log.error("oops", ex)
              }
            case Some (regStatus) =>
              log.debug(s"current regStatus: $regStatus")
              processMessage(tgId, message.text, regStatus)
          }
      }
    }
  }

  onCommand(BotCommands.update.name){ implicit message =>
    message.from match {
      case None => reply("Извините, какая-то ошибка")
      case Some(u) =>
        val tgId = u.id
        registerUserStepByStepService.getCurrent(tgId).map {
        case x@(None | Some(RegStatus(false, _))) =>
          reply (x.getOrElse(RegStatus(registered = false, UserRegStatus.notSaved)).status.message)
        case Some (regStatus) =>
          log.debug(s"current regStatus: $regStatus")
          reply ("Выберите параметр для редактирования", replyMarkup = Option(updateButtons))
      }
    }
  }

  // TODO
  onCommand(BotCommands.help.name){ implicit message =>
    reply("Этот раздел будет обязательно дописан когда-нибудь")
  }

  def processMessage (tgId:Long, message:Option[String], regStatus: RegStatus)(implicit msg:Message): Future[Any] = message match {
    case None       => reply (regStatus.status.message)
    case Some(text) =>
      registerUserStepByStepService.processMessage(tgId, regStatus, text) map {
        case Failure (ex)=>
          reply(s"${ex.getMessage}\n${regStatus.status.message}")
        case Success (_)=> Future.successful(registerUserStepByStepService.getNextStep(regStatus)) map {
          case x@UserRegStatus.hoursPerWeek=>
            reply (x.message, replyMarkup = Option(hoursButtons))
          case x =>
            reply(x.message)
        }
      }
  }
}