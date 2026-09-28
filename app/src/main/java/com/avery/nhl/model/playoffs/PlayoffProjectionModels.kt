package com.avery.nhl.model.playoffs

import com.avery.nhl.model.nhl.NHLPlayoffTeam


data class ProjectionWeights(
    val pointsPct: Double = 0.45,
    val goalDifferential: Double = 0.30,
    val recentForm: Double = 0.15,
    val homeIce: Double = 0.10
) {

    val total: Double
        get() =
            pointsPct +
                    goalDifferential +
                    recentForm +
                    homeIce
}


data class TeamProjectionStats(
    val teamAbbrev: String,

    val gamesPlayed: Int,

    val wins: Int,
    val losses: Int,
    val otLosses: Int,

    val points: Int,

    val goalDifferential: Int,

    val pointsPct: Double,

    val goalDifferentialPerGame: Double,

    val recentGames: Int,

    val recentWins: Int,

    val recentWinPct: Double
)


data class MatchupProjection(
    val topTeam: NHLPlayoffTeam,
    val bottomTeam: NHLPlayoffTeam,

    val topProbability: Double,
    val bottomProbability: Double,

    val topStrength: Double,
    val bottomStrength: Double,

    val projectedWinner: NHLPlayoffTeam,

    val projectedLoserWins: Int,

    val topStats: TeamProjectionStats?,
    val bottomStats: TeamProjectionStats?
)


data class TeamTournamentProjection(
    val team: NHLPlayoffTeam,

    val round2Probability: Double = 0.0,
    val conferenceFinalProbability: Double = 0.0,
    val cupFinalProbability: Double = 0.0,
    val cupWinProbability: Double = 0.0
)