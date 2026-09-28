package com.avery.nhl.util

fun formatNhlSeason(
    season: Int
): String {

    val value =
        season.toString()

    if (value.length != 8) {
        return value
    }

    val start =
        value.substring(0, 4)

    val end =
        value.substring(6, 8)

    return "$start-$end"
}

fun nhlTeamLogoUrl(
    teamAbbrev: String?
): String? {

    if (teamAbbrev.isNullOrBlank()) {
        return null
    }

    return "https://assets.nhle.com/logos/nhl/svg/${teamAbbrev.uppercase()}_light.svg"
}