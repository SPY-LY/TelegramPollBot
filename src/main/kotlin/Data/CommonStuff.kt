package Data

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div

class CommonStuff {
    companion object {
//        val resourcesPath: Path = Path("src\\main\\resources")
        val resourcesPath: Path = Path("resources")
        val firstLaunchFile: Path = (resourcesPath / "firstLaunch.conf")
        val supportedChatsFile: Path = resourcesPath / "supportedChats.conf"
        val botConfigFile: Path = resourcesPath / "bot.conf"
        val pollsFile : Path = resourcesPath / "polls.conf"
    }
}
