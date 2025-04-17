package sidim.doma.application.scheduler

import io.ktor.server.application.*
import org.quartz.JobBuilder
import org.quartz.SimpleScheduleBuilder
import org.quartz.TriggerBuilder
import org.quartz.impl.StdSchedulerFactory
import sidim.doma.application.scheduler.job.GameStatesJob
import sidim.doma.common.config.SchedulerConfig.GAME_STATES_JOB_DELAY
import sidim.doma.common.config.SchedulerConfig.GAME_STATES_START_JOB_DELAY
import java.time.Instant
import java.util.*

fun Application.configureGameStatesScheduler() {
    val scheduler = StdSchedulerFactory().scheduler

    val job = JobBuilder.newJob(GameStatesJob::class.java)
        .withIdentity("gameStatesJob", "gameStatesGroup")
        .build()

    val trigger = TriggerBuilder.newTrigger()
        .withIdentity("gameStatesTrigger", "gameStatesGroup")
        .startAt(Date.from(Instant.now().plusSeconds(GAME_STATES_START_JOB_DELAY)))
        .withSchedule(
            SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInHours(GAME_STATES_JOB_DELAY)
                .repeatForever()
        )
        .build()

    scheduler.scheduleJob(job, trigger)
    scheduler.start()

    monitor.subscribe(ApplicationStopping) {
        scheduler.shutdown(true)
    }
}