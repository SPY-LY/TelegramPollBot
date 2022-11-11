package ConfigEnjoyer

import Data.BotSettings
import Data.CommonStuff
import Data.PollSettings
import Data.SupportedChat
import com.beust.klaxon.Klaxon
import java.io.BufferedReader
import java.nio.file.Path
import kotlin.io.path.exists

class ConfigExtractor {
    companion object {
        fun getConfiguration(): BotSettings {
            return Klaxon().parse<BotSettings>(extractFrom(CommonStuff.botConfigFile))!!
        }

        fun getSupportedChats(): List<SupportedChat> {
            return Klaxon().parseArray(extractFrom(CommonStuff.supportedChatsFile))!!
        }

        fun isFirst() : Boolean {
            if (CommonStuff.firstLaunchFile.exists()) {
                return false
            }
            return true
        }

        private fun extractResourceFrom(resource : String): String? {
            return object {}.javaClass.getResource(resource)?.readText()
        }

        private fun extractFrom(resource: Path) : BufferedReader {
            return resource.toFile().bufferedReader()
        }

        fun getPolls(): List<PollSettings> {
            val a : List<PollSettings>? = Klaxon().parseArray(extractFrom(CommonStuff.pollsFile))
            return a!!
        }
    }
}