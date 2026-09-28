package com.avery.nhl.model.playoffs

import com.avery.nhl.model.nhl.NHLPlayoffBracket
import com.avery.nhl.model.nhl.NHLPlayoffSeries
import com.avery.nhl.model.nhl.NHLPlayoffTeam
import kotlin.collections.mapNotNull

import kotlin.random.Random

private data class SimulationCounter(
    val team: NHLPlayoffTeam,

    var round2: Int = 0,
    var conferenceFinal: Int = 0,
    var cupFinal: Int = 0,
    var cupWin: Int = 0
)

fun simulatePlayoffs(
    bracket: NHLPlayoffBracket,

    stats: Map<String, TeamProjectionStats>,

    settings: PlayoffSimulationSettings =
        PlayoffSimulationSettings()
): PlayoffSimulationResult {

    val firstRound =
        bracket.series
            .filter {
                it.playoffRound == 1
            }


    val teams =
        firstRound
            .flatMap {
                    series ->

                listOfNotNull(
                    series.topSeedTeam,
                    series.bottomSeedTeam
                )
            }
            .distinctBy {
                it.id
            }


    val counters =
        teams.associate {
                team ->

            team.id to
                    SimulationCounter(
                        team =
                            team
                    )
        }
            .toMutableMap()


    val random =
        settings.randomSeed
            ?.let {
                Random(it)
            }
            ?: Random.Default


    repeat(
        settings.iterations
            .coerceAtLeast(1)
    ) {

        simulateSingleBracket(
            bracket =
                bracket,

            stats =
                stats,

            weights =
                settings.weights,

            counters =
                counters,

            random =
                random
        )
    }


    val results =
        counters.values
            .map {
                    counter ->

                TeamSimulationResult(

                    team =
                        counter.team,

                    simulations =
                        settings.iterations,

                    round2Count =
                        counter.round2,

                    conferenceFinalCount =
                        counter.conferenceFinal,

                    cupFinalCount =
                        counter.cupFinal,

                    cupWinCount =
                        counter.cupWin
                )
            }
            .sortedByDescending {
                it.cupWinProbability
            }


    return PlayoffSimulationResult(
        iterations =
            settings.iterations,

        teams =
            results
    )
}

private fun simulateSingleBracket(
    bracket: NHLPlayoffBracket,
    stats: Map<String, TeamProjectionStats>,
    weights: ProjectionWeights,
    counters: MutableMap<Long, SimulationCounter>,
    random: Random
) {

    val firstRound =
        bracket.series
            .filter {
                it.playoffRound == 1
            }


    val eastSeries =
        firstRound
            .filter {
                it.conferenceAbbrev
                    ?.equals(
                        "E",
                        ignoreCase = true
                    ) == true
            }


    val westSeries =
        firstRound
            .filter {
                it.conferenceAbbrev
                    ?.equals(
                        "W",
                        ignoreCase = true
                    ) == true
            }


    val eastChampion =
        simulateConference(
            series =
                eastSeries,

            stats =
                stats,

            weights =
                weights,

            counters =
                counters,

            random =
                random
        )


    val westChampion =
        simulateConference(
            series =
                westSeries,

            stats =
                stats,

            weights =
                weights,

            counters =
                counters,

            random =
                random
        )


    if (
        eastChampion == null ||
        westChampion == null
    ) {
        return
    }


    counters.incrementCupFinal(
        eastChampion
    )

    counters.incrementCupFinal(
        westChampion
    )


    val cupWinner =
        simulateMatchup(
            teamA =
                westChampion,

            teamB =
                eastChampion,

            stats =
                stats,

            weights =
                weights,

            random =
                random
        )


    counters.incrementCupWin(
        cupWinner
    )
}

private fun MutableMap<Long, SimulationCounter>
        .incrementRound2(
    team: NHLPlayoffTeam
) {

    this[team.id]
        ?.let {
            it.round2++
        }
}


private fun MutableMap<Long, SimulationCounter>
        .incrementConferenceFinal(
    team: NHLPlayoffTeam
) {

    this[team.id]
        ?.let {
            it.conferenceFinal++
        }
}


private fun MutableMap<Long, SimulationCounter>
        .incrementCupFinal(
    team: NHLPlayoffTeam
) {

    this[team.id]
        ?.let {
            it.cupFinal++
        }
}


private fun MutableMap<Long, SimulationCounter>
        .incrementCupWin(
    team: NHLPlayoffTeam
) {

    this[team.id]
        ?.let {
            it.cupWin++
        }
}

private fun simulateConference(
    series: List<NHLPlayoffSeries>,

    stats: Map<String, TeamProjectionStats>,

    weights: ProjectionWeights,

    counters: MutableMap<Long, SimulationCounter>,

    random: Random
): NHLPlayoffTeam? {

    if (
        series.size < 4
    ) {
        return null
    }


    /*
     * Preserve bracket ordering from
     * the official NHL first-round series.
     */

    val firstRoundWinners =
        series.mapNotNull {
                matchup ->

            val top =
                matchup.topSeedTeam

            val bottom =
                matchup.bottomSeedTeam


            if (
                top == null ||
                bottom == null
            ) {

                null

            } else {

                simulateMatchup(
                    teamA =
                        top,

                    teamB =
                        bottom,

                    stats =
                        stats,

                    weights =
                        weights,

                    random =
                        random
                )
            }
        }


    if (
        firstRoundWinners.size != 4
    ) {
        return null
    }


    firstRoundWinners.forEach {
        counters.incrementRound2(
            it
        )
    }


    val secondRound1 =
        simulateMatchup(
            teamA =
                firstRoundWinners[0],

            teamB =
                firstRoundWinners[1],

            stats =
                stats,

            weights =
                weights,

            random =
                random
        )


    val secondRound2 =
        simulateMatchup(
            teamA =
                firstRoundWinners[2],

            teamB =
                firstRoundWinners[3],

            stats =
                stats,

            weights =
                weights,

            random =
                random
        )


    counters.incrementConferenceFinal(
        secondRound1
    )

    counters.incrementConferenceFinal(
        secondRound2
    )


    return simulateMatchup(
        teamA =
            secondRound1,

        teamB =
            secondRound2,

        stats =
            stats,

        weights =
            weights,

        random =
            random
    )
}

private fun simulateMatchup(
    teamA: NHLPlayoffTeam,
    teamB: NHLPlayoffTeam,

    stats: Map<String, TeamProjectionStats>,

    weights: ProjectionWeights,

    random: Random
): NHLPlayoffTeam {

    val aStats =
        stats[
            teamA.abbrev.uppercase()
        ]


    val bStats =
        stats[
            teamB.abbrev.uppercase()
        ]


    val projection =
        projectPlayoffMatchup(
            topTeam =
                teamA,

            bottomTeam =
                teamB,

            topStats =
                aStats,

            bottomStats =
                bStats,

            topHasHomeIce =
                determineHomeIce(
                    teamA =
                        teamA,

                    teamB =
                        teamB,

                    stats =
                        stats
                ),

            weights =
                weights
        )


    return if (
        random.nextDouble() <
        projection.topProbability
    ) {

        teamA

    } else {

        teamB
    }
}

private fun determineHomeIce(
    teamA: NHLPlayoffTeam,
    teamB: NHLPlayoffTeam,
    stats: Map<String, TeamProjectionStats>
): Boolean {

    val a =
        stats[
            teamA.abbrev.uppercase()
        ]

    val b =
        stats[
            teamB.abbrev.uppercase()
        ]


    if (
        a == null ||
        b == null
    ) {
        return true
    }


    return when {

        a.points >
                b.points ->

            true


        b.points >
                a.points ->

            false


        a.wins >
                b.wins ->

            true


        b.wins >
                a.wins ->

            false


        else ->

            true
    }
}