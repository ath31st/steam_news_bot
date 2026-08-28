package sidim.doma.infrastructure.plugin

import org.koin.core.Koin
import org.quartz.Job
import org.quartz.Scheduler
import org.quartz.spi.JobFactory
import org.quartz.spi.TriggerFiredBundle

class KoinJobFactory(private val koin: Koin) : JobFactory {
    override fun newJob(bundle: TriggerFiredBundle, scheduler: Scheduler): Job {
        return koin.get(bundle.jobDetail.jobClass.kotlin)
    }
}
