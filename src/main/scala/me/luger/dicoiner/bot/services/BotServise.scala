package me.luger.dicoiner.bot.services

import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{ChatActions, Polling, TelegramBot}
import info.mukel.telegrambot4s.models.Message

/**
  * @author luger. Created on 27.09.17.
  * @version ${VERSION}
  */
class BotServise(val token:String)
  extends TelegramBot
    with Polling
    with Commands
    with Callbacks{


  onCommand('start){implicit message =>
    reply("""Приветствую Вас.
         |Для регистрации Вам необходимо заполнить простую анкету.
         |Для начала Ваши имя и фамилия:""".stripMargin)
  }

  // TODO
  onCommand('help){ implicit message =>
    reply("")
  }

  override def receiveMessage(msg: Message): Unit = {

  }
}