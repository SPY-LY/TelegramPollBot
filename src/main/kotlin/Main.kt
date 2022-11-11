import ConfigEnjoyer.ConfigExtractor
import ConfigEnjoyer.ConfigModifier

fun program() {
    val bot = PollBot(ConfigExtractor.getConfiguration())
    println("bot started execution")
    bot.launch()
}

fun prebuild(args: Array<String>) {
    if (ConfigExtractor.isFirst()) {
        ConfigModifier.initiateFirstLaunch(args)
    }
}

fun main(args: Array<String>) {
    prebuild(args)
    program()
}