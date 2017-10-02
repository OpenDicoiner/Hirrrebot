package me.luger.dicoiner.bot.config

import com.typesafe.config.{Config, ConfigFactory}


/**
  * @author luger. Created on 02.10.17.
  * @version ${VERSION}
  */
case class GoogleFormConfig(
                             formUri:String,
                             bio: String,
                             phoneNumber: String,
                             email: String,
                             workStack :String,
                             ownStack : String,
                             minRate : String,
                             prefferedRate : String,
                             timeZone : String,
                             freeHours : String,
                             tgLink : String
                           )

object GoogleFormConfigLoad {
  def apply () : GoogleFormConfig = googleFormConfig
  def applyConfig ():GoogleFormConfig = GoogleFormConfig(
    formUri = config.getString("googleForm.formUri"),
    bio = config.getString("googleForm.bio"),
    phoneNumber = config.getString("googleForm.phoneNumber"),
    email = config.getString("googleForm.email"),
    workStack  = config.getString("googleForm.workStack"),
    ownStack  = config.getString("googleForm.ownStack"),
    minRate  = config.getString("googleForm.minRate"),
    prefferedRate  = config.getString("googleForm.prefferedRate"),
    timeZone  = config.getString("googleForm.timeZone"),
    freeHours  = config.getString("googleForm.freeHours"),
    tgLink  = config.getString("googleForm.tgLink")
  )

  private[this] lazy val config:Config = ConfigFactory.load()
  private[this] lazy val googleFormConfig:GoogleFormConfig =applyConfig()
}
