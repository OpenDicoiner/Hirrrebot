package me.luger.dicoiner.bot.model

import java.time.LocalDateTime

/**
  * @author luger. Created on 28.09.17.
  * @version ${VERSION}
  */
case class UserRegistration (tgUserId:Long,
                             registerCompleted:Boolean = false,
                             bioSaved : Boolean = false,
                             phoneNumberSaved : Boolean = false,
                             emailSaved : Boolean = false,
                             workingStackSaved:Boolean = false,
                             ownStackSaved:Boolean = false,
                             minRateSaved:Boolean = false,
                             prefferedRateSaved:Boolean = false,
                             timeZoneSaved:Boolean = false,
                             hoursPerWeekSaved:Boolean = false,
                             tgNickSaved:Boolean = false,
                             registerDate:Option[LocalDateTime])
