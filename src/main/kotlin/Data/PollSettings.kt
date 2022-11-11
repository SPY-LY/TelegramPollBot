package Data

import com.beust.klaxon.Json
import com.github.kotlintelegrambot.entities.User
import com.github.kotlintelegrambot.entities.polls.PollType

data class PollSettings(
    @Json(index = 0)val userFrom: User,
    @Json(index = 1)val chatId: Long,
    @Json(index = 2)val type: PollType,
    @Json(index = 3)val question: String,
    @Json(index = 6)val options: List<Option>,
    @Json(index = 5)val correctOptionId: Int? = null,
    @Json(index = 4)var pollId : Long? = null
)

