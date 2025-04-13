package sidim.doma.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Instant.formatted(): String = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy")
    .withZone(ZoneId.systemDefault())
    .format(this)