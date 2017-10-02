package me.luger.dicoiner.bot.services

import akka.actor.{Actor, ActorRef, Props}
import info.mukel.telegrambot4s.actors.ActorBroker
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.SendMessage
import info.mukel.telegrambot4s.models.{ChatId, InlineKeyboardButton, InlineKeyboardMarkup, Message}
import me.luger.dicoiner.bot.model.{BotCommands, HoursPerWeek, UserRegStatus}
import me.luger.dicoiner.bot.repositories.{FreelancerDAO, RegStatus, RegistrationDAO}
import org.slf4s.Logging

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
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
    with ActorBroker
    with Logging{

  override val broker: Option[ActorRef] = Some(system.actorOf(Props(new TwoWeeksSchedulerActor), "Actor"))
  implicit private val ec: ExecutionContextExecutor = system.dispatcher
  private val twoWeeksSeconds = 2 * 7 * 24 * 60
  system.scheduler.schedule(1000.milliseconds, twoWeeksSeconds.minutes)(broker.get!TwoWeeksSchedulerActor.NotifyUsers)

  val registerUserStepByStepService = new RegisterUserStepByStepService
  val googleFormSendService = new GoogleFormSendService
  val registrationDAO = new RegistrationDAO
  val freelancerDAO = new FreelancerDAO
  val HOURS_TAG = "HOURS_PER_WEEK"
  val UPDATE_TAG = "UPDATE_INFO"
  val UPDATE_WEEKLY_TAG = "UPDATE_WEEKLY"

  private def hoursTag = prefixTag(HOURS_TAG)_
  private def updTag = prefixTag(UPDATE_TAG)_
  private def updWeeklyTag = prefixTag(UPDATE_WEEKLY_TAG)_

  val hoursButtons: InlineKeyboardMarkup = InlineKeyboardMarkup.singleRow(HoursPerWeek.values.map{ x=>InlineKeyboardButton.callbackData(x.name, hoursTag (x.name))})

  val updateButtons: InlineKeyboardMarkup = InlineKeyboardMarkup.singleColumn(
    UserRegStatus.values
      .filter{x=> x != UserRegStatus.notSaved &&
        x != UserRegStatus.registered &&
        x != UserRegStatus.bio}
      .map{x=>InlineKeyboardButton.callbackData(x.updateMessage, updTag (x.name))}
  )

  val updWeeklySwitchButtons:InlineKeyboardMarkup =InlineKeyboardMarkup.singleColumn(
    Seq (InlineKeyboardButton.callbackData("Да", updWeeklyTag (1.toString)),
      InlineKeyboardButton.callbackData("Нет", updWeeklyTag (0.toString)))
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
          case Success (Some (user))=> Future.successful(registerUserStepByStepService.getNextStep(dd)) map {x =>
            log.info("check registered moment")
            googleFormSendService.sendForm(user).map(x => {
              log.debug(s"form response : $x")
              x
            })
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

  onCallbackWithTag(UPDATE_WEEKLY_TAG){implicit cbq =>
    ackCallback()
    val u = cbq.from
    val answer = cbq.data.getOrElse("0").toInt
    if (answer == 1)
      cbq.message.map { implicit msg =>
        registerUserStepByStepService.getCurrent(u.id).flatMap{x=>
          val dd = x.getOrElse(RegStatus(false, UserRegStatus.notSaved))
          val status = UserRegStatus.hoursPerWeek
          reply (status.message, replyMarkup = Option(hoursButtons))
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
          val chatId = message.chat.id
          registerUserStepByStepService.getCurrent(tgId).map {
            case None | Some( RegStatus(_, UserRegStatus.notSaved) ) =>
              log.info("save first")
              registerUserStepByStepService.saveFirst(tgId, chatId, tgNick).onComplete{
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
        case Success (Some(u))=> Future.successful(registerUserStepByStepService.getNextStep(regStatus)) map {
          case x@UserRegStatus.hoursPerWeek=>
            reply (x.message, replyMarkup = Option(hoursButtons))
          case x@UserRegStatus.registered =>
            log.info("check registered moment")
            googleFormSendService.sendForm(u).map(x => {
              log.debug(s"form response : ${x}")
              x
            })
            reply(x.message)
          case x =>
            log.debug(s"another reg status $x")
            reply(x.message)
        }
      }
  }

  class TwoWeeksSchedulerActor extends Actor {
    import TwoWeeksSchedulerActor._

    def receive: PartialFunction[Any, Unit] = {
      case NotifyUsers =>
        freelancerDAO.findAll.flatMap{users =>
          Future.sequence(users.map{ user =>
            request(
              SendMessage(
                chatId = ChatId.Chat(user.tgInfo.chatId),
                text = s"""Последние две недели вы доступны
           для работы ${user.hoursPerWeek.get.name} часов.
                  |Хотите обновить свою занятость?""".stripMargin,
                replyMarkup = Option(updWeeklySwitchButtons)
              )
            )
          })
        }
    }

  }

  object TwoWeeksSchedulerActor {
    case object NotifyUsers
  }
}
