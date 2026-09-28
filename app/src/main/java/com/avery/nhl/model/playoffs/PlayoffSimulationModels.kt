package com.avery.nhl.model.playoffs

import com.avery.nhl.model.nhl.NHLPlayoffTeam


data class PlayoffSimulationSettings(
    val iterations: Int = 10_000,

    val weights: ProjectionWeights =
        ProjectionWeights(),

    val randomSeed: Long? = null
)


data class TeamSimulationResult(
    val team: NHLPlayoffTeam,

    val simulations: Int,

    val round2Count: Int,
    val conferenceFinalCount: Int,
    val cupFinalCount: Int,
    val cupWinCount: Int
) {

    val round2Probability: Double
        get() =
            probability(
                round2Count
            )


    val conferenceFinalProbability: Double
        get() =
            probability(
                conferenceFinalCount
            )


    val cupFinalProbability: Double
        get() =
            probability(
                cupFinalCount
            )


    val cupWinProbability: Double
        get() =
            probability(
                cupWinCount
            )


    private fun probability(
        count: Int
    ): Double {

        return if (
            simulations > 0
        ) {

            count.toDouble() /
                    simulations

        } else {

            0.0
        }
    }
}


data class PlayoffSimulationResult(
    val iterations: Int,

    val teams: List<TeamSimulationResult>
)