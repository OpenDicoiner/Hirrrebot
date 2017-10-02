package me.luger.dicoiner.bot.services

import scalaj.http.Http
import me.luger.dicoiner.bot.config.{GoogleFormConfig, GoogleFormConfigLoad}
import me.luger.dicoiner.bot.model.Freelancer
import org.slf4s.Logging

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author luger. Created on 02.10.17.
  * @version ${VERSION}
  */
class GoogleFormSendService extends Logging{
  val config: GoogleFormConfig = GoogleFormConfigLoad()

  private def createRequest (freelancer: Freelancer): Seq[(String, String)] ={
    Seq (
      config.bio -> s"${freelancer.bio.name.getOrElse("")} ${freelancer.bio.surname.getOrElse("")}",
      config.phoneNumber -> freelancer.phoneNumber.getOrElse(""),
      config.email -> freelancer.email.getOrElse(""),
      config.workStack -> freelancer.ownTechStack.techs.mkString(""),
      config.ownStack -> freelancer.ownTechStack.techs.mkString(""),
      config.minRate -> freelancer.minRate.getOrElse(0).toString,
      config.prefferedRate -> freelancer.prefferedRate.getOrElse(0).toString,
      config.timeZone ->  freelancer.timeZone.map(_.getDisplayName).getOrElse(""),
      config.freeHours ->  freelancer.hoursPerWeek.map(_.name).getOrElse(""),
      config.tgLink -> freelancer.tgInfo.tgNick.map("t.me/" + _).getOrElse("")
    )
  }

  def sendForm (freelancer: Freelancer)(implicit ec: ExecutionContext)= {
    Future {Http(config.formUri).postForm(createRequest(freelancer)).asString}
  }
}
