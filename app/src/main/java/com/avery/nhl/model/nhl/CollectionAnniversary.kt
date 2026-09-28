package com.avery.nhl.model.nhl

import com.avery.nhl.model.jersey.Jersey
import java.time.LocalDate

data class CollectionAnniversary(
    val type: AnniversaryType,

    val jersey: Jersey?,
    val title: String,

    val originalDate: LocalDate,
    val occurrenceDate: LocalDate,

    val years: Int,
    val daysAway: Long
)