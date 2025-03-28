package sidim.doma.scheduler.job

import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.RawChatId
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import sidim.doma.entity.NewsItem
import sidim.doma.service.MessageService
import sidim.doma.service.NewsItemService
import sidim.doma.service.UserService
import java.time.Instant
import java.util.concurrent.CopyOnWriteArraySet

class NewsSenderJob : Job {
    override fun execute(context: JobExecutionContext) {
        runBlocking {
            val logger = LoggerFactory.getLogger("NewsSenderJob")
            val messageService = GlobalContext.get().get<MessageService>()
            val userService = GlobalContext.get().get<UserService>()
            val newsItemService = GlobalContext.get().get<NewsItemService>()
            val newsItems =
                GlobalContext.get().get<CopyOnWriteArraySet<NewsItem>>(named("newsItems"))

            if (newsItems.isEmpty()) {
                return@runBlocking
            }

            val startCycle = Instant.now()
            logger.info("Starting news sender job at {}", startCycle)
            logger.info("Found {} news items for sending", newsItems.size)

            coroutineScope {
                newsItems.flatMap { news ->
                    userService.getActiveUsersByAppId(news.appid).map { user -> news to user }
                }.map { (news, user) ->
                    logger.info("Sending news ${news.appid} to user ${user.chatId} (${user.steamId})")

                    launch {
                        try {
                            val chatId = ChatId(RawChatId(user.chatId.toLong()))
                            val newsText = newsItemService.formatNewsForTelegram(news, user.locale)
                            messageService.sendNewsMessage(
                                chatId = chatId,
                                text = newsText,
                                appid = news.appid,
                                locale = user.locale
                            )
                        } catch (e: Exception) {
                            logger.error("Failed to send news ${news.appid} to user ${user.chatId}: ${e.message}")
                        }
                    }
                }.joinAll()
            }

            logger.info(
                "News sender job completed in {} seconds",
                java.time.Duration.between(startCycle, Instant.now()).seconds
            )

            newsItems.clear()
        }
    }
}