package me.luger.dicoiner.bot
import me.luger.dicoiner.bot.config.ConfigLoad
import me.luger.dicoiner.bot.services.BotServise
import org.slf4s.Logging
/**
  * @author luger. Created on 27.09.17.
  * @version ${VERSION}
  */
object Main extends Logging{
  def main (args: Array[String]):Unit = {
    val token: String = ConfigLoad().botToken
    val botService = new BotServise(token)
    botService.run
  }
}
