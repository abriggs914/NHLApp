package com.avery.nhl.navigation

enum class AppDestination(
    val title: String
) {
    HOME("Home"),

    SCORES("Scores"),
    STANDINGS("Standings"),
    LEADERS("League Leaders"),

    PLAYOFFS("Playoffs"),
    PREDICTIONS("Predictions"),

    COLLECTION("Jersey Collection"),
    COLLECTION_STATS("Collection Stats"),

    JERSEY_SCHEDULE("Jersey Schedule"),
    HISTORY("NHL History")
}