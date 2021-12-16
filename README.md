# YuaBot

###Requirements
- Java 17

###Compiling
`./gradlew build`

### How to use
YuaBot has input classes and output classes.
Before using a class you have to configure it.

Example:
`java -jar yuabot.jar -name MyBot -setup dev.yuafox.yuabot.endpoints.TwitterEndpoint`

Available params:
- -name botName
- -setup className
- -input className
- -output className

Available classes:

Input:
- dev.yuafox.yuabot.sources.ImageSource

Output:
- dev.yuafox.yuabot.endpoints.TwitterEndpoint