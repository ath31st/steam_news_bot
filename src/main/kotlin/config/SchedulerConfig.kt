package sidim.doma.config

object SchedulerConfig {
    const val SEMAPHORE_LIMIT = 20
    const val CHUNK_SIZE = 10

    const val PROBLEM_GAMES_ATTEMPTS = 5
    const val PROBLEM_GAMES_INTERVAL_BETWEEN_ATTEMPTS = 1

    const val NEWS_FETCHER_JOB_INTERVAL = 30 // minutes
    const val NEWS_FETCHER_START_JOB_DELAY = 120L // seconds

    const val GAME_STATES_JOB_DELAY = 24 // hours
    const val GAME_STATES_START_JOB_DELAY = 60L // seconds
}