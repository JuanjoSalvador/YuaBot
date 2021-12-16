# YuaBot

## Requirements
- Java 17

## Compiling

Use the following code to compile

```bash
gradlew build
```

## How to use
YuaBot has input classes and output classes. Before using a class you have to configure it.


This example shows how to configure the Twitter output class for a bot named MyBot to be able to post tweets automatically:
```bash
java -jar yuabot.jar -name MyBot -setup dev.yuafox.yuabot.endpoints.TwitterEndpoint
```

### Available params:
```
-name botName
-setup className
-input className
-output className
```
### Available classes:

#### Input:
- dev.yuafox.yuabot.sources.ImageSource

#### Output:
- dev.yuafox.yuabot.endpoints.TwitterEndpoint

### Automate
Every time the bot is executed will do his job, post a Twitter photo for example.
You can make the bot post a photo every hour with crontab.

```cronjob
0 * * * * cd ~/bot && java -jar yuabot.jar <...>
```

## Example

Building a bot that posts a random picture to Twitter.

1. Setup the classes:
```
java -jar yuabot.jar -name TestBot -setup dev.yuafox.yuabot.sources.ImageSource dev.yuafox.yuabot.endpoints.TwitterEndpoint 
```
2. Put pictures in the folder bots/TestBot/images

3. Execute the following command:
```cronjob
java -jar yuabot.jar -input dev.yuafox.yuabot.sources.ImageSource -output dev.yuafox.yuabot.endpoints.TwitterEndpoint
```