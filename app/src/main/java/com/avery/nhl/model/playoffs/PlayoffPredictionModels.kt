package com.avery.nhl.model.playoffs

import com.avery.nhl.model.nhl.NHLPlayoffTeam


data class PlayoffPredictionBracket(
    val playoffYear: Int,
    val picks: Map<String, PlayoffSeriesPick> = emptyMap()
)


data class PlayoffSeriesPick(
    val seriesId: String,

    val topTeam: NHLPlayoffTeam?,
    val bottomTeam: NHLPlayoffTeam?,

    val predictedWinnerId: Long? = null,

    val predictedTopWins: Int? = null,
    val predictedBottomWins: Int? = null
) {

    val predictedWinner: NHLPlayoffTeam?
        get() =
            when (predictedWinnerId) {

                topTeam?.id ->
                    topTeam

                bottomTeam?.id ->
                    bottomTeam

                else ->
                    null
            }


    val isPicked: Boolean
        get() =
            predictedWinnerId != null
}

enum class PredictionConference {
    EAST,
    WEST
}


fun predictionSlotId(
    conference: PredictionConference,
    round: Int,
    index: Int
): String {

    return when {

        round == 4 ->
            "CUP"

        else ->
            "${conference.name}-R$round-$index"
    }
}