package com.avery.nhl.model.nhl

data class NHLGame(
    val id: Long,
    val gameDate: String,
    val startTimeUTC: String?,
    val gameState: String,
    val gameScheduleState: String?,

    val awayTeam: NHLGameTeam,
    val homeTeam: NHLGameTeam,

    val period: Int?,
    val periodType: String?,

    val clock: NHLGameClock?
) {

    val isLive: Boolean
        get() =
            gameState == "LIVE" ||
                    gameState == "CRIT"

    val isFinal: Boolean
        get() =
            gameState == "FINAL" ||
                    gameState == "OFF"

    val isPostponed: Boolean
        get() =
            gameState == "PPD"

    val isUpcoming: Boolean
        get() =
            !isLive &&
                    !isFinal &&
                    !isPostponed
}

data class NHLGameTeam(
    val abbrev: String,
    val name: String,
    val logo: String?,
    val score: Int?
)


data class NHLGameClock(
    val timeRemaining: String?,
    val inIntermission: Boolean,
    val running: Boolean
)