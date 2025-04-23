# Steam news bot

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)
![Steam](https://img.shields.io/badge/steam-%23000000.svg?style=for-the-badge&logo=steam&logoColor=white)
![SQLite](https://img.shields.io/badge/sqlite-%2307405e.svg?style=for-the-badge&logo=sqlite&logoColor=white)
![SonarLint](https://img.shields.io/badge/SonarLint-CB2029?style=for-the-badge&logo=sonarlint&logoColor=white)</br>

## List of contents

1. [Program version](#program-version)
2. [Introduction](#introduction)
3. [Project objectives](#program-version)
4. [What can the bot do?](#what-can-the-bot-do)
5. [List of supported commands](#list-of-supported-commands)
6. [List of used libraries](#list-of-used-libraries)
7. [Versions](#versions)
8. [Project Updates](#project-updates)
9. [License](#license)

## Program version

2.9.1

## Introduction

It's no secret that the steam platform has significant drawbacks and general slowness and
sluggishness. News about apps
and games there are in disarray and without sorting by date and time. This bot allows you to receive
the latest news
from application developers in a timely manner, in a convenient application (telegram)<br/>
![image info](images/image00.jpg)

![image info](images/image01.jpg)

Try it now: [Steam News Bot](https://t.me/steam_newsy_bot)

## Project objectives:

✅ Develop a telegram bot that will promptly deliver news on games and software from the steam
platform.<br/>
✅ Add the ability to ignore uninteresting applications.<br/>
✅ Place the bot on the linux(raspbian) server using PM2(advanced, production process manager for
node.js).<br/>
✅ Make the bot multilingual.<br/>

## What can the bot do?

1. The first and most important thing is that the bot does not use or store any private information
   about users. Only
   open sources and the steam API are used.
2. The bot determines the user's language from the set language in the telegram settings.
   Five languages are supported: ![](https://flagcdn.com/w20/ru.png)
   Russian, ![](https://flagcdn.com/w20/gb.png)English, ![](https://flagcdn.com/w20/fr.png)
   France, ![](https://flagcdn.com/w20/de.png)Germany, ![](https://flagcdn.com/w20/ua.png)Ukraine
   (updated 25.03.2025).
3. After the greeting, the bot will prompt the user to register. Registration is very simple, you
   only need a Steam ID.
   ![image info](images/image03.jpg)
4. When registering, the bot requests the user's application library from the steam, according to
   the entered Steam ID,
   and then saves the user's data to the database.
5. The bot has a scheduler.
    - Every 30 minutes, news is searched for and sent to users.
    - In case of problems (and the steam platform is not the
      most agile), problematic requests will be repeated every 5 minutes after failure.
    - Every 24 hours, the bot updates the user application database.
6. In the settings you can find:<br/>
   ![image info](images/image02.jpg)
    - _"Set/Update Steam ID"_ - This is necessary for registration.
    - _"Check your steam ID"_ - Here you can see the installed steam ID and activity mode.
    - _"Check available wishlist"_ - Checking that the wishlist is available and news on games from
      the list will be received.
    - _"Set \"active\" mode"_ - Set the active mode. Only in this mode the bot will send you news.
    - _"Set \"inactive\" mode"_ - Set inactive mode if you are tired of the news in general.
    - _"Clear black list"_ - Clearing the blacklist.
    - _"Black list"_ - List of blacklisted applications.
7. You can add the application to the blacklist under the news.<br/>
   ![image info](images/image04.jpg)
8. Blacklist management:<br/>
   ![image info](images/image09.jpg)
   ![image info](images/image10.jpg)
9. Added a wishlist for accounts that have access to game information in steam settings. In order
   for the bot to get access to the wishlist, the privacy settings in steam should look like
   this: (updated 21.08.2023)<br/>
   ![image info](images/image06.jpg)![image info](images/image07.jpg)
10. You can get links to the game by clicking under the news:<br/>
    ![image info](images/image05.jpg)
11. Show statistics for the bot (updated 13.04.2025).<br/>
    ![image info](images/image08.jpg)

## List of supported commands:

    /start
    /settings
    /stats
    /help

## List of used libraries:

1. telegram bot API - library for working with Telegram Bot API
2. slf4j + logback - logger
3. sqlite + exposed - lightweight database
4. quartz - job scheduling library
5. koin - dependency injection library
6. ktor - web framework
7. caffeine - caching library
8. flyway - database migrations

## Versions:

- Kotlin: 2.1.20</br>
- Ktor: 3.1.2</br>
- Koin: 4.0.4</br>
- SQLite: 3.49.1.0</br>
- Exposed: 0.60.0</br>
- Quartz: 2.5.0</br>
- TelegramBotAPI: 24.0.2</br>
- Caffeine (Aedile): 2.0.3</br>
- Flyway: 11.7.2</br>
- Gradle: 8.12</br>

You can use its services yourself if it is online - https://t.me/steam_newsy_bot.

## Project Updates

(update 21.08.2023)
I didn't rent a server for a bot, but just used ~~raspberries~~ mini-PC iRU 114. This is my little
production server
from improvised means.

(update 25.03.2025)
The application was completely rewritten from Java + Spring to Kotlin + Ktor using an asynchronous
approach.
It has become more stable and faster, and is prepared for an increase in the number of users.

(update 13.04.2025)
Added statistics for the bot.

(update 16.04.2025)
Pagination added for blacklist games.

## License

This project is distributed under the terms of the MIT license. See [LICENSE](LICENSE).