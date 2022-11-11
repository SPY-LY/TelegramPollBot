package Data

import com.beust.klaxon.Json
import com.github.kotlintelegrambot.entities.User


data class Option (
    @Json(index = 0)val answer: String,
    @Json(index = 1)val voters : MutableList<User> = ArrayList()
)