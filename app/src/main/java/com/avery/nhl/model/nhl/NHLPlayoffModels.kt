package com.avery.nhl.model.nhl


data class NHLPlayoffBracket(
    val year: Int,
    val bracketLogo: String?,
    val series: List<NHLPlayoffSeries>
)


data class NHLPlayoffSeries(
    val seriesLetter: String,
    val seriesTitle: String,
    val seriesAbbrev: String?,
    val playoffRound: Int,

    val conferenceAbbrev: String?,
    val conferenceName: String?,

    val topSeedRank: Int?,
    val topSeedRankAbbrev: String?,
    val topSeedWins: Int,

    val bottomSeedRank: Int?,
    val bottomSeedRankAbbrev: String?,
    val bottomSeedWins: Int,

    val winningTeamId: Long?,
    val losingTeamId: Long?,

    val topSeedTeam: NHLPlayoffTeam?,
    val bottomSeedTeam: NHLPlayoffTeam?
) {

    val isComplete: Boolean
        get() =
            winningTeamId != null ||
                    topSeedWins >= 4 ||
                    bottomSeedWins >= 4


    val winningTeam: NHLPlayoffTeam?
        get() =
            when (winningTeamId) {

                topSeedTeam?.id ->
                    topSeedTeam

                bottomSeedTeam?.id ->
                    bottomSeedTeam

                else ->
                    null
            }
}


data class NHLPlayoffTeam(
    val id: Long,
    val abbrev: String,
    val name: String,
    val commonName: String?,
    val logo: String?,
    val darkLogo: String?
)