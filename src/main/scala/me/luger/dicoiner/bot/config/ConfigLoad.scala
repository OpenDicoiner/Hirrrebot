package me.luger.dicoiner.bot.config

import com.typesafe.config.{ConfigFactory, Config}

/**
  * @author luger. Created on 27.09.17.
  * @version ${VERSION}
  */
case class BotConfig (botToken:String)

object ConfigLoad {
  def apply(): BotConfig = botConfig

  def applyConfig ():BotConfig = {
    val token = if (config.getString("bot-token").isEmpty)System.getenv("TELEGRAM_KEY") else config.getString("bot-token")
    BotConfig(token)
  }

  private[this] lazy val config:Config = ConfigFactory.load()
  private[this] lazy val botConfig:BotConfig =applyConfig()
}
