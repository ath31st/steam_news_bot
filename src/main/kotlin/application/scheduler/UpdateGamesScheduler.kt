package sidim.doma.application.scheduler

import io.ktor.server.application.*
import org.quartz.JobBuilder
import org.quartz.SimpleScheduleBuilder
import org.quartz.TriggerBuilder
import org.quartz.impl.StdSchedulerFactory
import sidim.doma.application.scheduler.config.SchedulerConfig.UPDATE_GAMES_JOB_DELAY
import sidim.doma.application.scheduler.config.SchedulerConfig.UPDATE_GAMES_START_JOB_DELAY
import sidim.doma.application.scheduler.job.UpdateGamesJob
import java.time.Instant
import java.util.*

fun Application.configureUpdateGamesScheduler() {
    val scheduler = StdSchedulerFactory().scheduler

    val job = JobBuilder.newJob(UpdateGamesJob::class.java)
        .withIdentity("updateGamesJob", "updateGamesGroup")
        .build()

    val trigger = TriggerBuilder.newTrigger()
        .withIdentity("updateGamesTrigger", "updateGamesGroup")
        .startAt(Date.from(Instant.now().plusSeconds(UPDATE_GAMES_START_JOB_DELAY)))
        .withSchedule(
            SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInHours(UPDATE_GAMES_JOB_DELAY)
                .repeatForever()
        )
        .build()

    scheduler.scheduleJob(job, trigger)
    scheduler.start()

    monitor.subscribe(ApplicationStopping) {
        scheduler.shutdown(true)
    }
}