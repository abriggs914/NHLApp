package com.avery.nhl.model.nhl

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun formatStartTime(
    utc: String
): String? {

    return try {

        val instant =
            Instant.parse(utc)

        val local =
            instant.atZone(
                ZoneId.systemDefault()
            )

        local.format(
            DateTimeFormatter.ofPattern(
                "h:mm a"
            )
        )

    } catch (
        _: Exception
    ) {

        null
    }
}