package com.avery.nhl.model.playoffs

import com.avery.nhl.model.nhl.NHLPlayoffBracket
import com.avery.nhl.model.nhl.NHLPlayoffSeries
import com.avery.nhl.model.nhl.NHLPlayoffTeam

fun buildInitialPredictionBracket(
    bracket: NHLPlayoffBracket
): PlayoffPredictionBracket {

    val firstRound =
        bracket.series
            .filter {
                it.playoffRound == 1
            }


    val west =
        firstRound
            .filter {
                it.conferenceAbbrev
                    ?.equals(
                        "W",
                        ignoreCase = true
                    ) == true
            }


    val east =
        firstRound
            .filter {
                it.conferenceAbbrev
                    ?.equals(
                        "E",
                        ignoreCase = true
                    ) == true
            }


    val picks =
        mutableMapOf<String, PlayoffSeriesPick>()


    west.forEachIndexed {
            index,
            series ->

        picks[
            predictionSlotId(
                PredictionConference.WEST,
                1,
                index
            )
        ] =
            series.toPredictionPick(
                predictionSlotId(
                    PredictionConference.WEST,
                    1,
                    index
                )
            )
    }


    east.forEachIndexed {
            index,
            series ->

        picks[
            predictionSlotId(
                PredictionConference.EAST,
                1,
                index
            )
        ] =
            series.toPredictionPick(
                predictionSlotId(
                    PredictionConference.EAST,
                    1,
                    index
                )
            )
    }


    return PlayoffPredictionBracket(
        playoffYear =
            bracket.year,

        picks =
            picks
    )
}

private fun NHLPlayoffSeries.toPredictionPick(
    slotId: String
): PlayoffSeriesPick {

    return PlayoffSeriesPick(
        seriesId =
            slotId,

        topTeam =
            topSeedTeam,

        bottomTeam =
            bottomSeedTeam
    )
}

fun rebuildPredictionBracket(
    bracket: PlayoffPredictionBracket
): PlayoffPredictionBracket {

    val picks =
        bracket.picks
            .toMutableMap()


    rebuildConference(
        picks =
            picks,

        conference =
            PredictionConference.WEST
    )


    rebuildConference(
        picks =
            picks,

        conference =
            PredictionConference.EAST
    )


    val westChampion =
        picks[
            predictionSlotId(
                PredictionConference.WEST,
                3,
                0
            )
        ]
            ?.predictedWinner


    val eastChampion =
        picks[
            predictionSlotId(
                PredictionConference.EAST,
                3,
                0
            )
        ]
            ?.predictedWinner


    val previousCup =
        picks["CUP"]


    picks["CUP"] =
        PlayoffSeriesPick(
            seriesId =
                "CUP",

            topTeam =
                westChampion,

            bottomTeam =
                eastChampion,

            predictedWinnerId =
                previousCup
                    ?.predictedWinnerId
                    ?.takeIf {
                            winnerId ->

                        winnerId ==
                                westChampion?.id ||
                                winnerId ==
                                eastChampion?.id
                    },

            predictedTopWins =
                previousCup
                    ?.predictedTopWins,

            predictedBottomWins =
                previousCup
                    ?.predictedBottomWins
        )


    return bracket.copy(
        picks =
            picks
    )
}

private fun rebuildConference(
    picks: MutableMap<String, PlayoffSeriesPick>,
    conference: PredictionConference
) {

    // Round 2
    for (
    index in 0 until 2
    ) {

        val first =
            picks[
                predictionSlotId(
                    conference,
                    1,
                    index * 2
                )
            ]
                ?.predictedWinner


        val second =
            picks[
                predictionSlotId(
                    conference,
                    1,
                    index * 2 + 1
                )
            ]
                ?.predictedWinner


        val id =
            predictionSlotId(
                conference,
                2,
                index
            )


        picks[id] =
            preserveValidPick(
                old =
                    picks[id],

                id =
                    id,

                top =
                    first,

                bottom =
                    second
            )
    }


    // Conference Final
    val finalist1 =
        picks[
            predictionSlotId(
                conference,
                2,
                0
            )
        ]
            ?.predictedWinner


    val finalist2 =
        picks[
            predictionSlotId(
                conference,
                2,
                1
            )
        ]
            ?.predictedWinner


    val finalId =
        predictionSlotId(
            conference,
            3,
            0
        )


    picks[finalId] =
        preserveValidPick(
            old =
                picks[finalId],

            id =
                finalId,

            top =
                finalist1,

            bottom =
                finalist2
        )
}

private fun preserveValidPick(
    old: PlayoffSeriesPick?,
    id: String,
    top: NHLPlayoffTeam?,
    bottom: NHLPlayoffTeam?
): PlayoffSeriesPick {

    val oldWinner =
        old?.predictedWinnerId


    val stillValid =
        oldWinner != null &&
                (
                        oldWinner == top?.id ||
                                oldWinner == bottom?.id
                        )


    return PlayoffSeriesPick(
        seriesId =
            id,

        topTeam =
            top,

        bottomTeam =
            bottom,

        predictedWinnerId =
            if (stillValid) {
                oldWinner
            } else {
                null
            },

        predictedTopWins =
            if (stillValid) {
                old?.predictedTopWins
            } else {
                null
            },

        predictedBottomWins =
            if (stillValid) {
                old?.predictedBottomWins
            } else {
                null
            }
    )
}

fun pickSeriesWinner(
    bracket: PlayoffPredictionBracket,
    seriesId: String,
    winnerId: Long
): PlayoffPredictionBracket {

    val existing =
        bracket.picks[
            seriesId
        ]
            ?: return bracket


    if (
        winnerId != existing.topTeam?.id &&
        winnerId != existing.bottomTeam?.id
    ) {
        return bracket
    }


    val updated =
        bracket.copy(
            picks =
                bracket.picks +
                        (
                                seriesId to
                                        existing.copy(
                                            predictedWinnerId =
                                                winnerId
                                        )
                                )
        )


    return rebuildPredictionBracket(
        updated
    )
}

fun setPredictedSeriesScore(
    bracket: PlayoffPredictionBracket,
    seriesId: String,
    loserWins: Int
): PlayoffPredictionBracket {

    if (
        loserWins !in 0..3
    ) {
        return bracket
    }


    val pick =
        bracket.picks[
            seriesId
        ]
            ?: return bracket


    val winner =
        pick.predictedWinnerId
            ?: return bracket


    val updated =
        if (
            winner ==
            pick.topTeam?.id
        ) {

            pick.copy(
                predictedTopWins = 4,
                predictedBottomWins = loserWins
            )

        } else {

            pick.copy(
                predictedTopWins = loserWins,
                predictedBottomWins = 4
            )
        }


    return bracket.copy(
        picks =
            bracket.picks +
                    (
                            seriesId to updated
                            )
    )
}

fun restorePredictionBracket(
    officialBracket: NHLPlayoffBracket,
    savedBracket: PlayoffPredictionBracket?
): PlayoffPredictionBracket {

    val fresh =
        buildInitialPredictionBracket(
            officialBracket
        )


    if (
        savedBracket == null ||
        savedBracket.playoffYear !=
        officialBracket.year
    ) {

        return fresh
    }


    var result =
        fresh


    /*
     * Restore Round 1 picks first.
     *
     * Later rounds are rebuilt from those
     * selections rather than trusting
     * persisted downstream teams.
     */

    fresh.picks
        .values
        .filter {
            "-R1-" in
                    it.seriesId
        }
        .forEach {
                freshPick ->

            val saved =
                savedBracket
                    .picks[
                    freshPick.seriesId
                ]
                    ?: return@forEach


            val winner =
                saved.predictedWinnerId
                    ?: return@forEach


            val valid =
                winner ==
                        freshPick.topTeam?.id ||
                        winner ==
                        freshPick.bottomTeam?.id


            if (
                valid
            ) {

                result =
                    pickSeriesWinner(
                        bracket =
                            result,

                        seriesId =
                            freshPick.seriesId,

                        winnerId =
                            winner
                    )


                val restored =
                    result.picks[
                        freshPick.seriesId
                    ]


                val loserWins =
                    when {

                        saved.predictedTopWins == 4 ->
                            saved.predictedBottomWins


                        saved.predictedBottomWins == 4 ->
                            saved.predictedTopWins


                        else ->
                            null
                    }


                if (
                    restored != null &&
                    loserWins != null
                ) {

                    result =
                        setPredictedSeriesScore(
                            bracket =
                                result,

                            seriesId =
                                freshPick.seriesId,

                            loserWins =
                                loserWins
                        )
                }
            }
        }


    /*
     * Round 1 selections have now generated
     * Round 2 teams.
     *
     * Restore later rounds in order.
     */

    for (
    round in 2..3
    ) {

        listOf(
            PredictionConference.WEST,
            PredictionConference.EAST
        )
            .forEach {
                    conference ->

                val count =
                    if (
                        round == 2
                    ) {
                        2
                    } else {
                        1
                    }


                for (
                index in 0 until count
                ) {

                    val id =
                        predictionSlotId(
                            conference,
                            round,
                            index
                        )


                    result =
                        restoreSavedPick(
                            bracket =
                                result,

                            savedBracket =
                                savedBracket,

                            seriesId =
                                id
                        )
                }
            }
    }


    result =
        restoreSavedPick(
            bracket =
                result,

            savedBracket =
                savedBracket,

            seriesId =
                "CUP"
        )


    return result
}

private fun restoreSavedPick(
    bracket: PlayoffPredictionBracket,
    savedBracket: PlayoffPredictionBracket,
    seriesId: String
): PlayoffPredictionBracket {

    val current =
        bracket.picks[
            seriesId
        ]
            ?: return bracket


    val saved =
        savedBracket.picks[
            seriesId
        ]
            ?: return bracket


    val winner =
        saved.predictedWinnerId
            ?: return bracket


    val valid =
        winner ==
                current.topTeam?.id ||
                winner ==
                current.bottomTeam?.id


    if (
        !valid
    ) {
        return bracket
    }


    var result =
        pickSeriesWinner(
            bracket =
                bracket,

            seriesId =
                seriesId,

            winnerId =
                winner
        )


    val loserWins =
        when {

            saved.predictedTopWins == 4 ->
                saved.predictedBottomWins

            saved.predictedBottomWins == 4 ->
                saved.predictedTopWins

            else ->
                null
        }


    if (
        loserWins != null
    ) {

        result =
            setPredictedSeriesScore(
                bracket =
                    result,

                seriesId =
                    seriesId,

                loserWins =
                    loserWins
            )
    }


    return result
}