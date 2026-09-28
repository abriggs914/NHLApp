package com.avery.nhl.data.playoffs

import android.content.Context

import com.avery.nhl.model.nhl.NHLPlayoffTeam

import com.avery.nhl.model.playoffs.PlayoffPredictionBracket
import com.avery.nhl.model.playoffs.PlayoffSavedState
import com.avery.nhl.model.playoffs.PlayoffSeriesPick
import com.avery.nhl.model.playoffs.ProjectionWeights

import org.json.JSONArray
import org.json.JSONObject

import java.io.File


class PlayoffStateRepository(
    context: Context
) {

    private val file =
        File(
            context.filesDir,
            FILE_NAME
        )


    fun load(
        playoffYear: Int
    ): PlayoffSavedState? {

        val root =
            readRoot()
                ?: return null


        val states =
            root.optJSONObject(
                "playoffs"
            )
                ?: return null


        val saved =
            states.optJSONObject(
                playoffYear.toString()
            )
                ?: return null


        return parseSavedState(
            playoffYear =
                playoffYear,

            obj =
                saved
        )
    }


    fun save(
        state: PlayoffSavedState
    ) {

        val root =
            readRoot()
                ?: JSONObject()


        val playoffs =
            root.optJSONObject(
                "playoffs"
            )
                ?: JSONObject()
                    .also {

                        root.put(
                            "playoffs",
                            it
                        )
                    }


        playoffs.put(
            state.playoffYear.toString(),
            savedStateToJson(
                state
            )
        )


        writeRoot(
            root
        )
    }


    fun clear(
        playoffYear: Int
    ) {

        val root =
            readRoot()
                ?: return


        val playoffs =
            root.optJSONObject(
                "playoffs"
            )
                ?: return


        playoffs.remove(
            playoffYear.toString()
        )


        writeRoot(
            root
        )
    }


    private fun readRoot(): JSONObject? {

        if (
            !file.exists()
        ) {
            return null
        }


        return try {

            val text =
                file.readText()


            if (
                text.isBlank()
            ) {
                null
            } else {
                JSONObject(
                    text
                )
            }

        } catch (_: Exception) {

            null
        }
    }


    private fun writeRoot(
        root: JSONObject
    ) {

        file.writeText(
            root.toString(
                2
            )
        )
    }


    companion object {

        private const val FILE_NAME =
            "playoff_state.json"
    }
}

private fun savedStateToJson(
    state: PlayoffSavedState
): JSONObject {

    return JSONObject().apply {

        put(
            "playoffYear",
            state.playoffYear
        )


        put(
            "projectionWeights",
            projectionWeightsToJson(
                state.projectionWeights
            )
        )


        put(
            "simulationIterations",
            state.simulationIterations
        )


        state.predictionBracket
            ?.let {

                put(
                    "predictionBracket",
                    predictionBracketToJson(
                        it
                    )
                )
            }
    }
}

private fun parseSavedState(
    playoffYear: Int,
    obj: JSONObject
): PlayoffSavedState {

    return PlayoffSavedState(

        playoffYear =
            playoffYear,


        predictionBracket =
            obj.optJSONObject(
                "predictionBracket"
            )
                ?.let {
                    parsePredictionBracket(
                        playoffYear =
                            playoffYear,

                        obj =
                            it
                    )
                },


        projectionWeights =
            obj.optJSONObject(
                "projectionWeights"
            )
                ?.let {
                    parseProjectionWeights(
                        it
                    )
                }
                ?: ProjectionWeights(),


        simulationIterations =
            obj.optInt(
                "simulationIterations",
                10_000
            )
    )
}

private fun projectionWeightsToJson(
    weights: ProjectionWeights
): JSONObject {

    return JSONObject().apply {

        put(
            "pointsPct",
            weights.pointsPct
        )

        put(
            "goalDifferential",
            weights.goalDifferential
        )

        put(
            "recentForm",
            weights.recentForm
        )

        put(
            "homeIce",
            weights.homeIce
        )
    }
}

private fun parseProjectionWeights(
    obj: JSONObject
): ProjectionWeights {

    return ProjectionWeights(

        pointsPct =
            obj.optDouble(
                "pointsPct",
                0.45
            ),

        goalDifferential =
            obj.optDouble(
                "goalDifferential",
                0.30
            ),

        recentForm =
            obj.optDouble(
                "recentForm",
                0.15
            ),

        homeIce =
            obj.optDouble(
                "homeIce",
                0.10
            )
    )
}

private fun predictionBracketToJson(
    bracket: PlayoffPredictionBracket
): JSONObject {

    val picks =
        JSONArray()


    bracket.picks
        .values
        .sortedBy {
            it.seriesId
        }
        .forEach {
                pick ->

            picks.put(
                seriesPickToJson(
                    pick
                )
            )
        }


    return JSONObject().apply {

        put(
            "playoffYear",
            bracket.playoffYear
        )

        put(
            "picks",
            picks
        )
    }
}

private fun seriesPickToJson(
    pick: PlayoffSeriesPick
): JSONObject {

    return JSONObject().apply {

        put(
            "seriesId",
            pick.seriesId
        )


        pick.topTeam
            ?.let {

                put(
                    "topTeam",
                    teamToJson(
                        it
                    )
                )
            }


        pick.bottomTeam
            ?.let {

                put(
                    "bottomTeam",
                    teamToJson(
                        it
                    )
                )
            }


        putNullable(
            "predictedWinnerId",
            pick.predictedWinnerId
        )


        putNullable(
            "predictedTopWins",
            pick.predictedTopWins
        )


        putNullable(
            "predictedBottomWins",
            pick.predictedBottomWins
        )
    }
}

private fun JSONObject.putNullable(
    name: String,
    value: Any?
) {

    if (
        value == null
    ) {

        put(
            name,
            JSONObject.NULL
        )

    } else {

        put(
            name,
            value
        )
    }
}

private fun teamToJson(
    team: NHLPlayoffTeam
): JSONObject {

    return JSONObject().apply {

        put(
            "id",
            team.id
        )

        put(
            "abbrev",
            team.abbrev
        )

        put(
            "name",
            team.name
        )

        putNullable(
            "commonName",
            team.commonName
        )

        putNullable(
            "logo",
            team.logo
        )

        putNullable(
            "darkLogo",
            team.darkLogo
        )
    }
}

private fun parsePredictionBracket(
    playoffYear: Int,
    obj: JSONObject
): PlayoffPredictionBracket {

    val picks =
        mutableMapOf<String, PlayoffSeriesPick>()


    val array =
        obj.optJSONArray(
            "picks"
        )


    if (
        array != null
    ) {

        for (
        index in 0 until array.length()
        ) {

            val item =
                array.optJSONObject(
                    index
                )
                    ?: continue


            val pick =
                parseSeriesPick(
                    item
                )


            if (
                pick.seriesId.isNotBlank()
            ) {

                picks[
                    pick.seriesId
                ] =
                    pick
            }
        }
    }


    return PlayoffPredictionBracket(

        playoffYear =
            playoffYear,

        picks =
            picks
    )
}

private fun parseSeriesPick(
    obj: JSONObject
): PlayoffSeriesPick {

    return PlayoffSeriesPick(

        seriesId =
            obj.optString(
                "seriesId",
                ""
            ),


        topTeam =
            obj.optJSONObject(
                "topTeam"
            )
                ?.let {
                    parseTeam(
                        it
                    )
                },


        bottomTeam =
            obj.optJSONObject(
                "bottomTeam"
            )
                ?.let {
                    parseTeam(
                        it
                    )
                },


        predictedWinnerId =
            obj.optNullableLong(
                "predictedWinnerId"
            ),


        predictedTopWins =
            obj.optNullableInt(
                "predictedTopWins"
            ),


        predictedBottomWins =
            obj.optNullableInt(
                "predictedBottomWins"
            )
    )
}

private fun JSONObject.optNullableLong(
    name: String
): Long? {

    if (
        !has(name) ||
        isNull(name)
    ) {
        return null
    }


    return optLong(
        name
    )
}


private fun JSONObject.optNullableInt(
    name: String
): Int? {

    if (
        !has(name) ||
        isNull(name)
    ) {
        return null
    }


    return optInt(
        name
    )
}

private fun parseTeam(
    obj: JSONObject
): NHLPlayoffTeam? {

    val id =
        obj.optLong(
            "id",
            0L
        )


    if (
        id <= 0
    ) {
        return null
    }


    return NHLPlayoffTeam(

        id =
            id,

        abbrev =
            obj.optString(
                "abbrev",
                "?"
            ),

        name =
            obj.optString(
                "name",
                "Unknown Team"
            ),

        commonName =
            obj.optNullableString(
                "commonName"
            ),

        logo =
            obj.optNullableString(
                "logo"
            ),

        darkLogo =
            obj.optNullableString(
                "darkLogo"
            )
    )
}

private fun JSONObject.optNullableString(
    name: String
): String? {

    if (
        !has(name) ||
        isNull(name)
    ) {
        return null
    }


    return optString(
        name
    )
        .takeIf {
            it.isNotBlank()
        }
}