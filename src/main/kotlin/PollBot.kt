import ConfigEnjoyer.ConfigExtractor
import ConfigEnjoyer.ConfigModifier
import Data.*
import com.beust.klaxon.Json
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.pollAnswer
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.User
import com.github.kotlintelegrambot.entities.polls.PollType
import com.github.kotlintelegrambot.logging.LogLevel
import java.util.*

class PollBot(val botSettings: BotSettings) {
    @Json(ignored = true)
    private val bot: Bot = bot {
        logLevel = LogLevel.Error
        token = botSettings.token
        dispatch {
            pollAnswer {
                println("${pollAnswer.user.username} has selected the option ${pollAnswer.optionIds} in the poll ${pollAnswer.pollId}")
                if (pollAnswer.optionIds.isEmpty()) {
                    ConfigModifier.removeContributorFromPoll(pollAnswer)
                    return@pollAnswer
                }
                ConfigModifier.addContributorToPoll(pollAnswer)
            }
            command("testPoll") {
                val chatId = getChatId(message)
                val poll = PollSettings(
                    message.from!!,
                    chatId!!, PollType.REGULAR,
                    "qq www", listOf(Option("1"), Option("2"))
                )
                createPoll(poll)
            }
            command("customPoll") {
                val chatId = getChatId(message)
                val parseResult = parseCustomPoll(message) ?: return@command
                val question: String = parseResult.first
                val options: List<Option> = parseResult.second
                val poll = PollSettings(
                    message.from!!,
                    chatId!!, PollType.REGULAR,
                    question, options
                )
                createPoll(poll)
            }
            command("initiate") {
                if (!testExistence(message)) {
                    return@command
                }
                val user = message.from
                val chat = message.chat
                val newChat = SupportedChat(user!!, chat)
                val chatList = ConfigExtractor.getSupportedChats()
                if (!chatList.any {
                        it.chat.id == newChat.chat.id
                    }) {
                    ConfigModifier.addChat(newChat)
                } else {
                    System.err.println("again you doing your shit...")
                    return@command
                }
                System.out.println("registrated new chat \r\n$newChat")
            }
            command("uninitiate") {
                TODO("Some useless shit to unsubscribe user from this chat")
            }
            command("getRandom") {
                if (!testExistence(message)) {
                    return@command
                }
                println("randomAccess $message")
                val settings = parseRandomSettings(message)
                if (!settings.valid) {
                    return@command
                }
                bringToDefaultExceptPoll(settings)
                val listToProceed = ConfigExtractor.getPolls().asReversed().filter {
                    it.userFrom.id == message.from!!.id
                }
                if (listToProceed.size <= settings.target!! || settings.target!! < 0) {
                    parseRandomSettingsFail(message, "Wrong poll index")
                    return@command
                }
                val pollToProceed = listToProceed[settings.target!!]
                bringToDefaultJustPoll(settings, pollToProceed)
                val chatToSend = getChatId(message)
                if (chatToSend == null) {
                    sendMessage(message, "to start with, initiate chat where to create polls and send partitions")
                    return@command
                }
                val randomPartition = createRandomPartition(pollToProceed, settings)
                bot.sendMessage(ChatId.fromId(chatToSend), randomPartition)
            }
        }
    }

    private fun parseCustomPoll(message: Message): Pair<String, List<Option>>? {
        var resultText: String? = null
        var resultOptions: List<Option>? = null
        val text = message.text
        if (text == null) {
            sendMessage(message, "idk, but there's no message")
            System.err.println("no text ${message}")
            return null
        }
        val commandsArgumets = text.split(" ").drop(1).joinToString(" ").split(Regex("\\s*-[a-zA-Z]+\\s+")).drop(1)
        val commands = text.split(" ").drop(1).joinToString(" ").split("-").map {
            it.split(" ").first()
        }.drop(1)
        if (commands.size != commandsArgumets.size) {
            sendMessage(message, "wrong arguments")
            return null
        }
        commands.forEachIndexed { index, command ->
            when (command) {
                "text" -> {
                    resultText = commandsArgumets[index]
                }

                "options" -> {
                    //TODO(пока что парсит по запятым)
                    resultOptions = commandsArgumets[index].split(Regex(",\\s*")).map { Option(it) }
                }

                else -> {
                    sendMessage(message, "wrong arguments")
                    return null
                }
            }
        }
        if (resultText == null || resultOptions == null) {
            sendMessage(message, "wrong arguments")
            return null
        }
        return Pair(resultText!!, resultOptions!!)
    }

    private fun createPoll(poll: PollSettings) {
        val sent = bot.sendPoll(
            chatId = ChatId.fromId(poll.chatId),
            type = poll.type,
            question = poll.question,
            options = poll.options.map { it.answer },
            correctOptionId = poll.correctOptionId,
            isAnonymous = false
        )
        sent.fold({
            poll.pollId = it.poll?.id
        }, {
            System.err.println("poll was not sent because of error ")
            return
        })
        ConfigModifier.addPoll(poll)
    }

    private fun bringToDefaultExceptPoll(settings: RandomSettings) {
        if (settings.target == null) {
            settings.target = 0
        }
        if (settings.outputFormat.isEmpty()) {
            settings.outputFormat.addAll(listOf(FormatFields.FirstName, FormatFields.LastName, FormatFields.Username))
        }
    }

    private fun bringToDefaultJustPoll(settings: RandomSettings, pollToProceed: PollSettings) {
        if (settings.pollFields.isEmpty()) {
            settings.pollFields.addAll(0..(pollToProceed.options.size - 1))
        }
    }

    private fun createRandomPartition(poll: PollSettings, settings: RandomSettings): String {
        val result: StringBuilder = StringBuilder()
        settings.pollFields.forEach { pollFieldInd ->
            result.append("${poll.options[pollFieldInd].answer}:\r\n")
            val contibutors: List<User> = getContributors(poll, pollFieldInd)
            contibutors.shuffled().forEachIndexed { index, user ->
                result.append("    $index) ")
                settings.outputFormat.forEach { field ->
                    result.append(
                        when (field) {
                            FormatFields.FirstName -> "${user.firstName} "

                            FormatFields.LastName -> if (user.lastName == null) "" else "${user.lastName} "

                            FormatFields.Username -> if (user.username != null) "@${user.username} " else ""
                        }
                    )
                }
                result.append("\r\n")
            }
        }
        return result.toString()
    }

    private fun getContributors(poll: PollSettings, pollFieldInd: Int): List<User> {
        return poll.options[pollFieldInd].voters
    }

    private fun testExistence(message: Message): Boolean {
        if (message.from == null) {
            sendMessage(message, "can't find u, sorry '(")
            System.err.println("wtff $message")
            return false
        }
        return true
    }


    private enum class RandomParserMode {
        Indexes, Format
    }

    enum class FormatFields {
        FirstName, LastName, Username
    }

    private fun parseRandomSettings(message: Message): RandomSettings {
        val keyWords = message.text?.split(Regex("\\s+"))?.drop(1) ?: throw NoSuchFieldError("kak tak to")
        val randomSettings = RandomSettings()
        var mode: RandomParserMode? = null
        keyWords.forEachIndexed { index, keyWord ->
            if (keyWord.startsWith('-')) {
                mode = null
                if (keyWord == "-this") {
                    randomSettings.target = 0
                } else if (keyWord == "-prev") {
                    randomSettings.target = -1
                } else if (keyWord.startsWith("-this-")) {
                    try {
                        randomSettings.target = -keyWord.drop(5).toInt()
                    } catch (e: NumberFormatException) {
                        return parseRandomSettingsFail(message, "wrong target format")
                    }
                } else if (keyWord == "-target") {
                    mode = RandomParserMode.Indexes
                } else if (keyWord == "-format") {
                    mode = RandomParserMode.Format
                }
            } else {
                when (mode) {
                    RandomParserMode.Indexes -> {
                        if (keyWord.last() == ',') {
                            try {
                                randomSettings.pollFields.add(keyWord.dropLast(1).toInt())
                            } catch (e: NumberFormatException) {
                                return parseRandomSettingsFail(message, "wrong arguments format (1)")
                            }
                        } else {
                            mode = null
                            try {
                                randomSettings.pollFields.add(keyWord.toInt())
                            } catch (e: NumberFormatException) {
                                return parseRandomSettingsFail(message, "wrong arguments format (2)")
                            }
                        }
                    }

                    null -> {
                        return parseRandomSettingsFail(message, "wrong arguments format (3)")
                    }

                    RandomParserMode.Format -> {
                        when (keyWord.lowercase(Locale.getDefault())) {
                            "username" -> {
                                randomSettings.outputFormat.add(FormatFields.Username)
                            }

                            "firstname" -> {
                                randomSettings.outputFormat.add(FormatFields.FirstName)
                            }

                            "lastname" -> {
                                randomSettings.outputFormat.add(FormatFields.LastName)
                            }

                            else -> {
                                return parseRandomSettingsFail(message, "wrong arguments format (4)")
                            }
                        }
                    }
                }
            }
        }
        return randomSettings
    }

    private fun parseRandomSettingsFail(message: Message, failText: String): RandomSettings {
        sendMessage(message, failText)
        val settings = RandomSettings()
        settings.valid = false
        return settings
    }

    private fun sendMessage(message: Message, text: String) {
        bot.sendMessage(ChatId.fromId(message.chat.id), text)
    }

    private fun getChatId(message: Message): Long? {
        val found = ConfigExtractor.getSupportedChats().find {
            it.user.id == message.from?.id
        }
        if (found == null) {
            System.err.println("why are you not regestered hmmm $message")
            sendMessage(message, "why are you not regestered hmmm (Security exception)")
            return null
        }
        return found.chat.id
    }


    fun launch() {
        bot.startPolling()
    }

}

