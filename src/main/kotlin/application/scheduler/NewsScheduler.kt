package sidim.doma.application.scheduler

import io.ktor.server.application.*
import org.quartz.JobBuilder
import org.quartz.SimpleScheduleBuilder
import org.quartz.TriggerBuilder
import org.quartz.impl.StdSchedulerFactory
import org.quartz.listeners.JobChainingJobListener
import sidim.doma.application.scheduler.job.NewsFetcherJob
import sidim.doma.application.scheduler.job.NewsSenderJob
import sidim.doma.application.scheduler.job.ProblemGamesJob
import sidim.doma.application.scheduler.config.SchedulerConfig.NEWS_FETCHER_JOB_INTERVAL
import sidim.doma.application.scheduler.config.SchedulerConfig.NEWS_FETCHER_START_JOB_DELAY
import java.time.Instant
import java.util.*

fun Application.configureNewsScheduler() {
    val scheduler = StdSchedulerFactory().scheduler

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
    scheduler.start()

    monitor.subscribe(ApplicationStopping) {
        scheduler.shutdown(true)
    }
}