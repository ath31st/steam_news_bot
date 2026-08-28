package sidim.doma.application.scheduler

import io.ktor.server.application.*
import org.koin.ktor.ext.getKoin
import org.quartz.JobBuilder
import org.quartz.SimpleScheduleBuilder
import org.quartz.TriggerBuilder
import org.quartz.impl.StdSchedulerFactory
import org.quartz.listeners.JobChainingJobListener
import sidim.doma.application.scheduler.config.SchedulerConfig.GAME_STATES_JOB_DELAY
import sidim.doma.application.scheduler.config.SchedulerConfig.GAME_STATES_START_JOB_DELAY
import sidim.doma.application.scheduler.config.SchedulerConfig.NEWS_FETCHER_JOB_INTERVAL
import sidim.doma.application.scheduler.config.SchedulerConfig.NEWS_FETCHER_START_JOB_DELAY
import sidim.doma.application.scheduler.config.SchedulerConfig.UPDATE_GAMES_JOB_DELAY
import sidim.doma.application.scheduler.config.SchedulerConfig.UPDATE_GAMES_START_JOB_DELAY
import sidim.doma.application.scheduler.job.GameStatesJob
import sidim.doma.application.scheduler.job.NewsFetcherJob
import sidim.doma.application.scheduler.job.NewsSenderJob
import sidim.doma.application.scheduler.job.ProblemGamesJob
import sidim.doma.application.scheduler.job.UpdateGamesJob
import sidim.doma.infrastructure.plugin.KoinJobFactory
import java.time.Instant
import java.util.*

fun Application.configureSchedulers() {
    val koin = getKoin()
    val scheduler = StdSchedulerFactory.getDefaultScheduler()
    scheduler.setJobFactory(KoinJobFactory(koin))

    val fetcherJob = JobBuilder.newJob(NewsFetcherJob::class.java)
        .withIdentity("newsFetcherJob", "newsGroup")
        .build()

    val fetcherTrigger = TriggerBuilder.newTrigger()
        .withIdentity("newsFetcherTrigger", "newsGroup")
        .startAt(Date.from(Instant.now().plusSeconds(NEWS_FETCHER_START_JOB_DELAY)))
        .withSchedule(
            SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMinutes(NEWS_FETCHER_JOB_INTERVAL)
                .repeatForever()
        )
        .build()

    val problemGamesJob = JobBuilder.newJob(ProblemGamesJob::class.java)
        .withIdentity("problemGamesJob", "newsGroup")
        .storeDurably()
        .build()

    val senderJob = JobBuilder.newJob(NewsSenderJob::class.java)
        .withIdentity("newsSenderJob", "newsGroup")
        .storeDurably()
        .build()

    val chainingListener = JobChainingJobListener("newsChain")
    chainingListener.addJobChainLink(fetcherJob.key, problemGamesJob.key)
    chainingListener.addJobChainLink(problemGamesJob.key, senderJob.key)

    scheduler.listenerManager.addJobListener(chainingListener)
    scheduler.scheduleJob(fetcherJob, fetcherTrigger)
    scheduler.addJob(problemGamesJob, false)
    scheduler.addJob(senderJob, false)

    val gameStatesJob = JobBuilder.newJob(GameStatesJob::class.java)
        .withIdentity("gameStatesJob", "gameStatesGroup")
        .build()

    val gameStatesTrigger = TriggerBuilder.newTrigger()
        .withIdentity("gameStatesTrigger", "gameStatesGroup")
        .startAt(Date.from(Instant.now().plusSeconds(GAME_STATES_START_JOB_DELAY)))
        .withSchedule(
            SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInHours(GAME_STATES_JOB_DELAY)
                .repeatForever()
        )
        .build()

    scheduler.scheduleJob(gameStatesJob, gameStatesTrigger)

    val updateGamesJob = JobBuilder.newJob(UpdateGamesJob::class.java)
        .withIdentity("updateGamesJob", "updateGamesGroup")
        .build()

    val updateGamesTrigger = TriggerBuilder.newTrigger()
        .withIdentity("updateGamesTrigger", "updateGamesGroup")
        .startAt(Date.from(Instant.now().plusSeconds(UPDATE_GAMES_START_JOB_DELAY)))
        .withSchedule(
            SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInHours(UPDATE_GAMES_JOB_DELAY)
                .repeatForever()
        )
        .build()

    scheduler.scheduleJob(updateGamesJob, updateGamesTrigger)

    scheduler.start()

    monitor.subscribe(ApplicationStopping) {
        scheduler.shutdown(true)
    }
}
