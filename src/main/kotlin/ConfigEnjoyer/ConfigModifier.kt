package ConfigEnjoyer

import Data.CommonStuff
import Data.PollSettings
import Data.SupportedChat
import com.beust.klaxon.Klaxon
import com.github.kotlintelegrambot.entities.polls.PollAnswer
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.writeText

class ConfigModifier {
    companion object {
        fun addChat(chat: SupportedChat) {
            val listOfChats = ConfigExtractor.getSupportedChats().toMutableList()
            listOfChats.add(chat)
            CommonStuff.supportedChatsFile.writeText(Klaxon().toJsonString(listOfChats))
        }

        fun clearArray(file: Path) {
            file.writeText("[]")
        }

        fun initiateFirstLaunch(args: Array<String>) {
            if (!CommonStuff.resourcesPath.exists()) {
                CommonStuff.resourcesPath.createDirectory()
            }
            val botConfigFile = CommonStuff.botConfigFile
            val token: String
            if (args.isEmpty()) {
                val scanner = Scanner(System.`in`)
                println("enter bot token: ")
                token = scanner.next()
            } else {
                token = args[0]
            }
            if (!botConfigFile.exists()) {
                botConfigFile.createFile()
            }
            botConfigFile.writeText(
                "{\n" +
                        "    \"token\" : \"${token}\"\r\n" +
                        "}"
            )
            val firstLaunchFile = CommonStuff.firstLaunchFile
            firstLaunchFile.createFile()
            firstLaunchFile.writeText("{1}")

            val supportedChatsFile = CommonStuff.supportedChatsFile
            if (!supportedChatsFile.exists()) {
                supportedChatsFile.createFile()
            }
            clearArray(supportedChatsFile)

            ConfigExtractor.getConfiguration()

            val pollsFile = CommonStuff.pollsFile
            if (!pollsFile.exists()) {
                pollsFile.createFile()
            }
            clearArray(pollsFile)
        }

        fun addPoll(poll: PollSettings) {
            val listOfPolls = ConfigExtractor.getPolls().toMutableList()
            listOfPolls.add(poll)
            CommonStuff.pollsFile.writeText(Klaxon().toJsonString(listOfPolls))
        }

        fun addContributorToPoll(pollAnswer: PollAnswer) {
            val listOfPolls = ConfigExtractor.getPolls().toMutableList()
            listOfPolls.forEachIndexed { index, pollSettings ->
                if (pollSettings.pollId!! == pollAnswer.pollId.toLong()) {
                    pollAnswer.optionIds.forEach {
                        pollSettings.options[it].voters.add(pollAnswer.user)
                    }
                }
            }
            CommonStuff.pollsFile.writeText(Klaxon().toJsonString(listOfPolls))
        }

        fun removeContributorFromPoll(pollAnswer: PollAnswer) {
            val listOfPolls = ConfigExtractor.getPolls().toMutableList()
            listOfPolls.forEachIndexed { _, pollSettings ->
                if (pollSettings.pollId!! == pollAnswer.pollId.toLong()) {
                    pollSettings.options.forEach {
                        it.voters.removeAll { it == pollAnswer.user }
                    }
                }
            }
            CommonStuff.pollsFile.writeText(Klaxon().toJsonString(listOfPolls))

            //this function same as above except remove part
        }
    }
}