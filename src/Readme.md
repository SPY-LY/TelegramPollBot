Rules working with bot
1) add to needed chat and initiate him for yourself in chat you want him to work with (firstly you need to add this bot to this chat) - write /initiate command
2) create poll - use command /customPoll with arguments
   1) -text following with text that will appear as question in poll (avoid - in text as it appears to be arguments parsing symbol)
   2) -options following with options that users will choose from (separate them with comma)
3) launch randomGenerator using /getRandom command with arguments
   1) -this, -prev, -this-x (where x index of targeted poll from tail 0 indexed) or even nothing (-this by default)
   2) -target followed by indexes of targeted answers of poll (format x1, x2, x3, ..., xn) (all of the answers by default will be included)
   3) -format followed by fields which needs to output (by default firstname secondname @username)

Notes to bot host
1) to start bot - launch botStarter.cmd file. If it is your first launch then enter bot token in console
2) bot can launch and relaunch at any time. It will memorise all the input from users 
3) need to clear polls like once per long time. Yet there is no self-cleaning function. Files to clear: supportedChats.conf, polls.conf