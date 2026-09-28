package com.avery.nhl.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit

val monthNames = listOf(
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December"
)

data class SimpleDateParts(
    val year: Int,
    val month: Int
)

fun extractDateParts(
    value: String?
): SimpleDateParts? {

    if (value.isNullOrBlank() || value.length < 7) {
        return null
    }

    val year =
        value.substring(0, 4)
            .toIntOrNull()
            ?: return null

    val month =
        value.substring(5, 7)
            .toIntOrNull()
            ?: return null

    if (month !in 1..12) {
        return null
    }

    return SimpleDateParts(
        year = year,
        month = month
    )
}

fun parseCollectionDate(
    value: String?
): LocalDate? {

    if (value.isNullOrBlank()) {
        return null
    }

    return try {

        // Handles:
        // 2025-04-23
        // 2025-04-23T00:00:00
        // etc.
        LocalDate.parse(
            value.take(10)
        )

    } catch (_: Exception) {
        null
    }
}

fun daysBetween(
    start: String?,
    end: String?
): Long? {

    val startDate =
        parseCollectionDate(start)
            ?: return null

    val endDate =
        parseCollectionDate(end)
            ?: return null

    return ChronoUnit.DAYS.between(
        startDate,
        endDate
    )
}