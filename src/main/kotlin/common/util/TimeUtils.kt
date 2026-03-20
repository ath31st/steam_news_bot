package sidim.doma.common.util

fun isNewsRecent(
    newsTimestamp: Long, timeWindow: Long, currentTime: Long = System.currentTimeMillis() / 1000,
): Boolean {
    return newsTimestamp >= (currentTime - timeWindow)
}