package me.luger.dicoiner.bot.services

import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.{Marshal, Marshaller, PredefinedToRequestMarshallers}
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
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
  val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

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

  def sendForm (freelancer: Freelancer)(implicit ec: ExecutionContext, system:akka.actor.ActorSystem, materializer:akka.stream.ActorMaterializer ) : Future[String] =
    for {
      request <- {
        log.debug(s"${mapper.writeValueAsString(createRequest(freelancer))}")
        Marshal( createRequest(freelancer)).to[RequestEntity]
      }
      response <- Http().singleRequest(HttpRequest(method = HttpMethods.POST, uri = config.formUri, entity = request.withContentType(ContentTypes.`application/json`)))
      entity <- Unmarshal(response.entity).to[String]

    } yield entity
}
