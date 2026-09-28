package com.avery.nhl.model.nhl


data class NHLGameDetail(
    val id: Long,
    val gameState: String,

    val awayTeam: NHLGameTeam,
    val homeTeam: NHLGameTeam,

    val period: Int?,
    val periodType: String?,
    val clock: NHLGameClock?,

    val venue: String?,
    val attendance: Int?,

    val scoringPeriods: List<NHLScoringPeriod>,
    val threeStars: List<NHLGameStar>
) {

    val isLive: Boolean
        get() =
            gameState == "LIVE" ||
                    gameState == "CRIT"

    val isFinal: Boolean
        get() =
            gameState == "FINAL" ||
                    gameState == "OFF"
}


data class NHLScoringPeriod(
    val period: Int,
    val periodType: String?,
    val goals: List<NHLGoal>
)


data class NHLGoal(
    val playerId: Long?,
    val firstName: String?,
    val lastName: String?,

    val teamAbbrev: String?,
    val headshot: String?,

    val timeInPeriod: String?,

    val goalsToDate: Int?,

    val awayScore: Int?,
    val homeScore: Int?,

    val strength: String?,
    val shotType: String?,

    val assists: List<NHLGoalAssist>
) {

    val playerName: String
        get() =
            listOfNotNull(
                firstName,
                lastName
            )
                .filter {
                    it.isNotBlank()
                }
                .joinToString(" ")
                .ifBlank {
                    "Unknown Player"
                }
}


data class NHLGoalAssist(
    val playerId: Long?,
    val name: String,
    val assistsToDate: Int?
)


data class NHLGameStar(
    val playerId: Long?,
    val name: String,
    val teamAbbrev: String?,
    val position: String?,
    val headshot: String?
)

data class NHLGameTeamStats(
    val away: NHLSingleTeamGameStats,
    val home: NHLSingleTeamGameStats
)


data class NHLSingleTeamGameStats(
    val shots: Int?,
    val faceoffPct: Double?,
    val powerPlayGoals: Int?,
    val powerPlayOpportunities: Int?,
    val hits: Int?,
    val blockedShots: Int?,
    val giveaways: Int?,
    val takeaways: Int?
)


data class NHLGameGoalie(
    val playerId: Long?,
    val name: String,
    val sweaterNumber: Int?,
    val shotsAgainst: Int?,
    val saves: Int?,
    val goalsAgainst: Int?,
    val savePct: Double?,
    val toi: String?
)