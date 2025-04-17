package sidim.doma.common.config

import dev.inmo.kslog.common.KSLog
import dev.inmo.kslog.common.LogLevel
import dev.inmo.kslog.common.defaultMessageFormatter
import kotlin.coroutines.cancellation.CancellationException

fun configureLogging() {
    KSLog.default =
        KSLog { level: LogLevel, tag: String?, message: Any, throwable: Throwable? ->
            if (throwable is CancellationException) return@KSLog
            if (level == LogLevel.ERROR || level == LogLevel.WARNING) {
                println(defaultMessageFormatter(level, tag, message, throwable))
            }
        }
}
