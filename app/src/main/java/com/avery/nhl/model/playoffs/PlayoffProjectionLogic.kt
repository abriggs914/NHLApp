package com.avery.nhl.model.playoffs

import com.avery.nhl.model.nhl.NHLGame
import com.avery.nhl.model.nhl.NHLPlayoffTeam
import com.avery.nhl.model.nhl.NHLStandingsTeam

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

fun recentTeamRecord(
    teamAbbrev: String,
    games: List<NHLGame>,
    count: Int = 10
): Pair<Int, Int> {

    val completed =
        games
            .filter {
                it.isFinal
            }
            .sortedByDescending {
                it.gameDate
            }
            .take(
                count
            )


    var wins = 0


    completed.forEach {
            game ->

        val team =
            teamAbbrev.uppercase()


        val away =
            game.awayTeam.abbrev
                .uppercase()


        val home =
            game.homeTeam.abbrev
                .uppercase()


        val awayScore =
            game.awayTeam.score


        val homeScore =
            game.homeTeam.score


        if (
            awayScore == null ||
            homeScore == null
        ) {
            return@forEach
        }


        val won =
            when (team) {

                away ->
                    awayScore > homeScore

                home ->
                    homeScore > awayScore

                else ->
                    false
            }


        if (won) {
            wins++
        }
    }


    return wins to completed.size
}

fun buildTeamProjectionStats(
    standings: NHLStandingsTeam,
    seasonGames: List<NHLGame>,
    recentGameCount: Int = 10
): TeamProjectionStats {

    val (
        recentWins,
        recentGames
    ) =
        recentTeamRecord(
            teamAbbrev =
                standings.teamAbbrev,

            games =
                seasonGames,

            count =
                recentGameCount
        )


    return TeamProjectionStats(

        teamAbbrev =
            standings.teamAbbrev,


        gamesPlayed =
            standings.gamesPlayed,


        wins =
            standings.wins,


        losses =
            standings.losses,


        otLosses =
            standings.otLosses,


        points =
            standings.points,


        goalDifferential =
            standings.goalDifferential ?: 0,


        pointsPct =
            standings.pointsPercentage,


        goalDifferentialPerGame =
            standings.goalDifferentialPerGame,


        recentGames =
            recentGames,


        recentWins =
            recentWins,


        recentWinPct =
            if (
                recentGames > 0
            ) {

                recentWins.toDouble() /
                        recentGames

            } else {

                0.5
            }
    )
}

private fun normalizeGoalDifferential(
    value: Double
): Double {

    /*
     * Roughly map:
     *
     * -2.0 goals/game -> 0
     *  0.0            -> .5
     * +2.0            -> 1
     */

    return (
            0.5 +
                    value / 4.0
            )
        .coerceIn(
            0.0,
            1.0
        )
}

fun teamProjectionStrength(
    stats: TeamProjectionStats,
    hasHomeIce: Boolean,
    weights: ProjectionWeights =
        ProjectionWeights()
): Double {

    val normalizedGoalDiff =
        normalizeGoalDifferential(
            stats.goalDifferentialPerGame
        )


    val homeValue =
        if (hasHomeIce) {
            1.0
        } else {
            0.0
        }


    val weighted =
        stats.pointsPct *
                weights.pointsPct +

                normalizedGoalDiff *
                weights.goalDifferential +

                stats.recentWinPct *
                weights.recentForm +

                homeValue *
                weights.homeIce


    return if (
        weights.total > 0.0
    ) {

        weighted /
                weights.total

    } else {

        0.5
    }
}

private fun strengthProbability(
    topStrength: Double,
    bottomStrength: Double
): Double {

    val difference =
        topStrength -
                bottomStrength


    val scale =
        6.0


    return 1.0 /
            (
                    1.0 +
                            exp(
                                -scale *
                                        difference
                            )
                    )
}

private fun clampProjectionProbability(
    value: Double
): Double {

    return value.coerceIn(
        0.15,
        0.85
    )
}

fun projectPlayoffMatchup(
    topTeam: NHLPlayoffTeam,
    bottomTeam: NHLPlayoffTeam,

    topStats: TeamProjectionStats?,
    bottomStats: TeamProjectionStats?,

    topHasHomeIce: Boolean = true,

    weights: ProjectionWeights =
        ProjectionWeights()
): MatchupProjection {

    val topStrength =
        topStats
            ?.let {

                teamProjectionStrength(
                    stats =
                        it,

                    hasHomeIce =
                        topHasHomeIce,

                    weights =
                        weights
                )
            }
            ?: 0.5


    val bottomStrength =
        bottomStats
            ?.let {

                teamProjectionStrength(
                    stats =
                        it,

                    hasHomeIce =
                        !topHasHomeIce,

                    weights =
                        weights
                )
            }
            ?: 0.5


    val topProbability =
        clampProjectionProbability(
            strengthProbability(
                topStrength =
                    topStrength,

                bottomStrength =
                    bottomStrength
            )
        )


    val bottomProbability =
        1.0 -
                topProbability


    val projectedWinner =
        if (
            topProbability >=
            bottomProbability
        ) {

            topTeam

        } else {

            bottomTeam
        }


    val winnerProbability =
        max(
            topProbability,
            bottomProbability
        )


    val loserWins =
        probabilityToSeriesLosses(
            winnerProbability
        )


    return MatchupProjection(

        topTeam =
            topTeam,

        bottomTeam =
            bottomTeam,


        topProbability =
            topProbability,

        bottomProbability =
            bottomProbability,


        topStrength =
            topStrength,

        bottomStrength =
            bottomStrength,


        projectedWinner =
            projectedWinner,

        projectedLoserWins =
            loserWins,


        topStats =
            topStats,

        bottomStats =
            bottomStats
    )
}

private fun probabilityToSeriesLosses(
    winnerProbability: Double
): Int {

    return when {

        winnerProbability >= 0.75 ->
            0

        winnerProbability >= 0.65 ->
            1

        winnerProbability >= 0.56 ->
            2

        else ->
            3
    }
}

fun modelAgreement(
    pick: PlayoffSeriesPick?,
    projection: MatchupProjection?
): Boolean? {

    val userWinner =
        pick
            ?.predictedWinnerId
            ?: return null


    val modelWinner =
        projection
            ?.projectedWinner
            ?.id
            ?: return null


    return userWinner ==
            modelWinner
}