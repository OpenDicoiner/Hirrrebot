package me.luger.dicoiner.bot.services
import me.luger.dicoiner.bot.model._
import me.luger.dicoiner.bot.repositories.FreelancerDAO
import me.luger.dicoiner.bot.repositories.RegistrationDAO
import org.bson.BsonObjectId

/**
  * @author luger. Created on 28.09.17.
  * @version ${VERSION}
  */
class RegisterUserStepByStepService {
  private val freelancerDAO = new FreelancerDAO
  private val registrationDAO = new RegistrationDAO

  def saveBio (tgId:Long, name:String, surname:String) = {
    for {
      freelancer <- freelancerDAO.getByTgId(tgId)
      saved <- freelancer match {
        case None => freelancerDAO.save(
          Freelancer(
            _id = BsonObjectId,
            bio = Bio(
              name = freelancer.get("name").map(x => x).bsonToString(),
              surname = freelancer.get("surname").bsonToString()
            ),
            phoneNumber = freelancer.get("phoneNumber").bsonToString(),
            email = freelancer.get("email").bsonToString(),
            tgInfo = TgInfo(
              tgId = tgId,
              tgNick = freelancer.get("tgNick").bsonToString()
            ),
            workingTechStack = TechStack(
              freelancer.get("workingTechStack").bsonArrayToSeq()
            ),
            ownTechStack = TechStack(
              freelancer.get("ownTechStack").bsonArrayToSeq()
            ),
            minRate = freelancer.get("minRate").bsonToDouble(),
            prefferedRate = freelancer.get("prefferedRate").bsonToDouble(),
            timeZone = freelancer.get("timeZone").bsonToTimeZone(),
            hoursPerWeek = freelancer.get("hoursPerWeek")
              .map(x => HoursPerWeek(x.asString().getValue)).get
          )
        )
      }
    }
  }
}
