package com.avery.nhl.model.nhl


data class NHLBoxscore(
    val gameId: Long,

    val awayTeam: NHLBoxscoreTeam,
    val homeTeam: NHLBoxscoreTeam
)


data class NHLBoxscoreTeam(
    val abbrev: String,
    val score: Int?,
    val shotsOnGoal: Int?,

    val forwards: List<NHLBoxscoreSkater>,
    val defense: List<NHLBoxscoreSkater>,
    val goalies: List<NHLBoxscoreGoalie>
) {

    val skaters: List<NHLBoxscoreSkater>
        get() =
            forwards + defense
}


data class NHLBoxscoreSkater(
    val playerId: Long?,
    val name: String,
    val sweaterNumber: Int?,
    val position: String?,

    val goals: Int?,
    val assists: Int?,
    val points: Int?,
    val plusMinus: Int?,

    val shotsOnGoal: Int?,
    val hits: Int?,
    val blockedShots: Int?,

    val pim: Int?,
    val toi: String?
)


data class NHLBoxscoreGoalie(
    val playerId: Long?,
    val name: String,
    val sweaterNumber: Int?,

    val shotsAgainst: Int?,
    val saves: Int?,
    val goalsAgainst: Int?,

    val savePct: Double?,
    val toi: String?,

    val decision: String?
)