package sidim.doma.common.util

import kotlinx.datetime.Clock

fun isNewsRecent(
    newsTimestamp: Long, timeWindow: Long, currentTime: Long = Clock.System.now().epochSeconds,
): Boolean {
    return newsTimestamp >= (currentTime - timeWindow)
}