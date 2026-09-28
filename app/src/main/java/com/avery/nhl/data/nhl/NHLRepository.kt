package com.avery.nhl.data.nhl

import com.avery.nhl.model.nhl.NHLGame
import com.avery.nhl.model.nhl.NHLGameClock
import com.avery.nhl.model.nhl.NHLGameTeam
import com.avery.nhl.model.nhl.NHLGameDetail
import com.avery.nhl.model.nhl.NHLGameStar
import com.avery.nhl.model.nhl.NHLGoal
import com.avery.nhl.model.nhl.NHLGoalAssist
import com.avery.nhl.model.nhl.NHLScoringPeriod
import com.avery.nhl.model.nhl.NHLBoxscore
import com.avery.nhl.model.nhl.NHLBoxscoreGoalie
import com.avery.nhl.model.nhl.NHLBoxscoreSkater
import com.avery.nhl.model.nhl.NHLBoxscoreTeam
import com.avery.nhl.model.nhl.NHLStandings
import com.avery.nhl.model.nhl.NHLStandingsTeam
import com.avery.nhl.model.nhl.NHLPlayerProfile
import com.avery.nhl.model.nhl.NHLPlayoffBracket
import com.avery.nhl.model.nhl.NHLPlayoffSeries
import com.avery.nhl.model.nhl.NHLPlayoffSeriesDetail
import com.avery.nhl.model.nhl.NHLPlayoffTeam
import com.avery.nhl.model.nhl.NHLSeasonStats
import com.avery.nhl.model.nhl.NHLStatLeader
import com.avery.nhl.model.nhl.teamNameToAbbrev
import com.avery.nhl.model.playoffs.TeamProjectionStats
import com.avery.nhl.model.playoffs.buildTeamProjectionStats

import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import org.json.JSONObject


class NHLRepository {

    companion object {
        private const val API_BASE =
            "https://api-web.nhle.com/v1"
    }

    suspend fun getScores(
        date: LocalDate
    ): List<NHLGame> {

        val json =
            getJson(
                "$API_BASE/score/$date"
            )

        val gamesJson =
            json.optJSONArray("games")
                ?: return emptyList()

        val games =
            mutableListOf<NHLGame>()

        for (
        index in 0 until gamesJson.length()
        ) {

            val obj =
                gamesJson.getJSONObject(index)

            games.add(
                parseGame(obj)
            )
        }

        return games
    }

    private fun parseGame(
        obj: JSONObject
    ): NHLGame {

        val away =
            parseTeam(
                obj.optJSONObject("awayTeam")
            )

        val home =
            parseTeam(
                obj.optJSONObject("homeTeam")
            )

        val periodDescriptor =
            obj.optJSONObject(
                "periodDescriptor"
            )

        val clockJson =
            obj.optJSONObject("clock")

        val clock =
            if (clockJson != null) {

                NHLGameClock(
                    timeRemaining =
                        clockJson
                            .optNullableString(
                                "timeRemaining"
                            ),

                    inIntermission =
                        clockJson.optBoolean(
                            "inIntermission",
                            false
                        ),

                    running =
                        clockJson.optBoolean(
                            "running",
                            false
                        )
                )

            } else {
                null
            }

        return NHLGame(
            id =
                obj.optLong("id"),

            gameDate =
                obj.optString("gameDate"),

            startTimeUTC =
                obj.optNullableString(
                    "startTimeUTC"
                ),

            gameState =
                obj.optString("gameState"),

            gameScheduleState =
                obj.optNullableString(
                    "gameScheduleState"
                ),

            awayTeam = away,
            homeTeam = home,

            period =
                periodDescriptor
                    ?.optInt("number")
                    ?.takeIf { it > 0 },

            periodType =
                periodDescriptor
                    ?.optNullableString(
                        "periodType"
                    ),

            clock = clock
        )
    }

    private fun parseTeam(
        obj: JSONObject?
    ): NHLGameTeam {

        if (obj == null) {
            return NHLGameTeam(
                abbrev = "?",
                name = "Unknown",
                logo = null,
                score = null
            )
        }

        val abbrev =
            obj.optString(
                "abbrev",
                "?"
            )

        val name =
            obj.optJSONObject("name")
                ?.optNullableString(
                    "default"
                )
                ?: abbrev

        val score =
            if (
                obj.has("score") &&
                !obj.isNull("score")
            ) {
                obj.optInt("score")
            } else {
                null
            }

        val logo =
            obj.optNullableString("logo")
                ?: abbrev
                    .takeIf {
                        it.isNotBlank() &&
                                it != "?"
                    }
                    ?.let {
                        "https://assets.nhle.com/logos/nhl/svg/${it}_dark.svg"
                    }

        return NHLGameTeam(
            abbrev = abbrev,
            name = name,
            logo = logo,
            score = score
        )
    }

    private suspend fun getJson(
        url: String
    ): JSONObject {

        return withContext(
            Dispatchers.IO
        ) {

            val connection =
                URL(url)
                    .openConnection()
                        as HttpURLConnection

            try {

                connection.requestMethod = "GET"

                connection.connectTimeout =
                    10_000

                connection.readTimeout =
                    10_000

                connection.setRequestProperty(
                    "Accept",
                    "application/json"
                )

                val status =
                    connection.responseCode

                if (
                    status !in 200..299
                ) {
                    throw Exception(
                        "NHL API returned HTTP $status"
                    )
                }

                val text =
                    connection
                        .inputStream
                        .bufferedReader()
                        .use {
                            it.readText()
                        }

                JSONObject(text)

            } finally {

                connection.disconnect()
            }
        }
    }

    suspend fun getGameDetail(
        gameId: Long
    ): NHLGameDetail {

        val json =
            getJson(
                "$API_BASE/gamecenter/$gameId/landing"
            )

        return parseGameDetail(json)
    }

    private fun parseGameDetail(
        obj: JSONObject
    ): NHLGameDetail {

        val periodDescriptor =
            obj.optJSONObject(
                "periodDescriptor"
            )

        val clockJson =
            obj.optJSONObject("clock")

        val clock =
            if (clockJson != null) {

                NHLGameClock(
                    timeRemaining =
                        clockJson.optNullableString(
                            "timeRemaining"
                        ),

                    inIntermission =
                        clockJson.optBoolean(
                            "inIntermission",
                            false
                        ),

                    running =
                        clockJson.optBoolean(
                            "running",
                            false
                        )
                )

            } else {
                null
            }


        val summary =
            obj.optJSONObject("summary")


        val gameInfo =
            summary
                ?.optJSONObject("gameInfo")


        val venue =
            obj.optJSONObject("venue")
                ?.optNullableLocalizedString()
                ?: gameInfo
                    ?.optJSONObject("venue")
                    ?.optNullableLocalizedString()


        val attendance =
            if (
                gameInfo != null &&
                gameInfo.has("attendance") &&
                !gameInfo.isNull("attendance")
            ) {
                gameInfo.optInt("attendance")
            } else {
                null
            }


        return NHLGameDetail(
            id =
                obj.optLong("id"),

            gameState =
                obj.optString(
                    "gameState"
                ),

            awayTeam =
                parseTeam(
                    obj.optJSONObject(
                        "awayTeam"
                    )
                ),

            homeTeam =
                parseTeam(
                    obj.optJSONObject(
                        "homeTeam"
                    )
                ),

            period =
                periodDescriptor
                    ?.optInt("number")
                    ?.takeIf {
                        it > 0
                    },

            periodType =
                periodDescriptor
                    ?.optNullableString(
                        "periodType"
                    ),

            clock = clock,

            venue = venue,

            attendance = attendance,

            scoringPeriods =
                parseScoring(
                    summary
                        ?.optJSONArray(
                            "scoring"
                        )
                ),

            threeStars =
                parseThreeStars(
                    gameInfo
                        ?.optJSONArray(
                            "threeStars"
                        )
                )
        )
    }

    private fun parseScoring(
        periodsJson: org.json.JSONArray?
    ): List<NHLScoringPeriod> {

        if (periodsJson == null) {
            return emptyList()
        }

        val periods =
            mutableListOf<NHLScoringPeriod>()


        for (
        i in 0 until periodsJson.length()
        ) {

            val periodObj =
                periodsJson.optJSONObject(i)
                    ?: continue


            val periodDescriptor =
                periodObj.optJSONObject(
                    "periodDescriptor"
                )


            val periodNumber =
                periodDescriptor
                    ?.optInt(
                        "number",
                        i + 1
                    )
                    ?: (i + 1)


            val periodType =
                periodDescriptor
                    ?.optNullableString(
                        "periodType"
                    )


            val goalsJson =
                periodObj.optJSONArray("goals")


            val goals =
                mutableListOf<NHLGoal>()


            if (goalsJson != null) {

                for (
                j in 0 until goalsJson.length()
                ) {

                    val goalObj =
                        goalsJson.optJSONObject(j)
                            ?: continue

                    goals.add(
                        parseGoal(goalObj)
                    )
                }
            }


            periods.add(
                NHLScoringPeriod(
                    period = periodNumber,
                    periodType = periodType,
                    goals = goals
                )
            )
        }


        return periods
    }

    private fun parseGoal(
        obj: JSONObject
    ): NHLGoal {

        val assists =
            mutableListOf<NHLGoalAssist>()


        val assistsJson =
            obj.optJSONArray("assists")


        if (assistsJson != null) {

            for (
            i in 0 until assistsJson.length()
            ) {

                val assistObj =
                    assistsJson.optJSONObject(i)
                        ?: continue

                assists.add(
                    NHLGoalAssist(
                        playerId =
                            assistObj
                                .optLong("playerId")
                                .takeIf {
                                    it > 0
                                },

                        name =
                            assistObj
                                .optJSONObject(
                                    "name"
                                )
                                ?.optNullableLocalizedString()
                                ?: "Unknown",

                        assistsToDate =
                            assistObj
                                .optNullableInt(
                                    "assistsToDate"
                                )
                    )
                )
            }
        }


        return NHLGoal(
            playerId =
                obj.optLong("playerId")
                    .takeIf {
                        it > 0
                    },

            firstName =
                obj.optJSONObject(
                    "firstName"
                )
                    ?.optNullableLocalizedString(),

            lastName =
                obj.optJSONObject(
                    "lastName"
                )
                    ?.optNullableLocalizedString(),

            teamAbbrev =
                obj.optString(
                    "teamAbbrev"
                )
                    .takeIf {
                        it.isNotBlank()
                    },

            headshot =
                obj.optNullableString(
                    "headshot"
                ),

            timeInPeriod =
                obj.optNullableString(
                    "timeInPeriod"
                ),

            goalsToDate =
                obj.optNullableInt(
                    "goalsToDate"
                ),

            awayScore =
                obj.optNullableInt(
                    "awayScore"
                ),

            homeScore =
                obj.optNullableInt(
                    "homeScore"
                ),

            strength =
                obj.optNullableString(
                    "strength"
                ),

            shotType =
                obj.optNullableString(
                    "shotType"
                ),

            assists = assists
        )
    }

    private fun parseThreeStars(
        starsJson: org.json.JSONArray?
    ): List<NHLGameStar> {

        if (starsJson == null) {
            return emptyList()
        }

        val stars =
            mutableListOf<NHLGameStar>()


        for (
        i in 0 until starsJson.length()
        ) {

            val obj =
                starsJson.optJSONObject(i)
                    ?: continue


            val playerId =
                obj.optLong(
                    "playerId"
                )
                    .takeIf {
                        it > 0
                    }


            val teamAbbrev =
                obj.optNullableString(
                    "teamAbbrev"
                )


            val headshot =
                obj.optNullableString(
                    "headshot"
                )
                    ?: if (
                        playerId != null &&
                        !teamAbbrev.isNullOrBlank()
                    ) {

                        // Fallback only.
                        null

                    } else {
                        null
                    }


            stars.add(
                NHLGameStar(
                    playerId =
                        playerId,

                    name =
                        obj.optJSONObject(
                            "name"
                        )
                            ?.optNullableLocalizedString()
                            ?: "Unknown Player",

                    teamAbbrev =
                        teamAbbrev,

                    position =
                        obj.optNullableString(
                            "position"
                        ),

                    headshot =
                        headshot
                )
            )
        }


        return stars
    }
//
//    suspend fun getGameBoxscore(
//        gameId: Long
//    ): JSONObject {
//
//        return getJson(
//            "$API_BASE/gamecenter/$gameId/boxscore"
//        )
//    }

    suspend fun getGameBoxscore(
        gameId: Long
    ): NHLBoxscore {

        return parseBoxscore(
            getJson(
                "$API_BASE/gamecenter/$gameId/boxscore"
            )
        )
    }

    private fun parseBoxscore(
        obj: JSONObject
    ): NHLBoxscore {

        val playerStats =
            obj.optJSONObject(
                "playerByGameStats"
            )


        val awayStats =
            playerStats
                ?.optJSONObject(
                    "awayTeam"
                )


        val homeStats =
            playerStats
                ?.optJSONObject(
                    "homeTeam"
                )


        return NHLBoxscore(
            gameId =
                obj.optLong("id"),

            awayTeam =
                parseBoxscoreTeam(
                    teamObj =
                        obj.optJSONObject(
                            "awayTeam"
                        ),

                    statsObj =
                        awayStats
                ),

            homeTeam =
                parseBoxscoreTeam(
                    teamObj =
                        obj.optJSONObject(
                            "homeTeam"
                        ),

                    statsObj =
                        homeStats
                )
        )
    }

    private fun parseBoxscoreTeam(
        teamObj: JSONObject?,
        statsObj: JSONObject?
    ): NHLBoxscoreTeam {

        val abbrev =
            teamObj
                ?.optNullableString(
                    "abbrev"
                )
                ?: "?"


        val score =
            teamObj
                ?.optNullableInt(
                    "score"
                )


        val shots =
            teamObj
                ?.optNullableInt(
                    "sog"
                )


        return NHLBoxscoreTeam(
            abbrev = abbrev,
            score = score,
            shotsOnGoal = shots,

            forwards =
                parseSkaters(
                    statsObj
                        ?.optJSONArray(
                            "forwards"
                        )
                ),

            defense =
                parseSkaters(
                    statsObj
                        ?.optJSONArray(
                            "defense"
                        )
                ),

            goalies =
                parseGoalies(
                    statsObj
                        ?.optJSONArray(
                            "goalies"
                        )
                )
        )
    }

    private fun parseSkaters(
        array: org.json.JSONArray?
    ): List<NHLBoxscoreSkater> {

        if (array == null) {
            return emptyList()
        }


        val result =
            mutableListOf<NHLBoxscoreSkater>()


        for (
        i in 0 until array.length()
        ) {

            val obj =
                array.optJSONObject(i)
                    ?: continue


            val name =
                obj.optJSONObject("name")
                    ?.optNullableLocalizedString()
                    ?: "Unknown Player"


            result.add(
                NHLBoxscoreSkater(
                    playerId =
                        obj.optLong(
                            "playerId"
                        )
                            .takeIf {
                                it > 0
                            },

                    name =
                        name,

                    sweaterNumber =
                        obj.optNullableInt(
                            "sweaterNumber"
                        ),

                    position =
                        obj.optNullableString(
                            "position"
                        ),

                    goals =
                        obj.optNullableInt(
                            "goals"
                        ),

                    assists =
                        obj.optNullableInt(
                            "assists"
                        ),

                    points =
                        obj.optNullableInt(
                            "points"
                        ),

                    plusMinus =
                        obj.optNullableInt(
                            "plusMinus"
                        ),

                    shotsOnGoal =
                        obj.optNullableInt(
                            "sog"
                        ),

                    hits =
                        obj.optNullableInt(
                            "hits"
                        ),

                    blockedShots =
                        obj.optNullableInt(
                            "blockedShots"
                        ),

                    pim =
                        obj.optNullableInt(
                            "pim"
                        ),

                    toi =
                        obj.optNullableString(
                            "toi"
                        )
                )
            )
        }


        return result
    }

    private fun parseGoalies(
        array: org.json.JSONArray?
    ): List<NHLBoxscoreGoalie> {

        if (array == null) {
            return emptyList()
        }


        val result =
            mutableListOf<NHLBoxscoreGoalie>()


        for (
        i in 0 until array.length()
        ) {

            val obj =
                array.optJSONObject(i)
                    ?: continue


            result.add(
                NHLBoxscoreGoalie(
                    playerId =
                        obj.optLong(
                            "playerId"
                        )
                            .takeIf {
                                it > 0
                            },

                    name =
                        obj.optJSONObject(
                            "name"
                        )
                            ?.optNullableLocalizedString()
                            ?: "Unknown Goalie",

                    sweaterNumber =
                        obj.optNullableInt(
                            "sweaterNumber"
                        ),

                    shotsAgainst =
                        obj.optNullableInt(
                            "shotsAgainst"
                        ),

                    saves =
                        obj.optNullableInt(
                            "saves"
                        ),

                    goalsAgainst =
                        obj.optNullableInt(
                            "goalsAgainst"
                        ),

                    savePct =
                        obj.optNullableDouble(
                            "savePctg"
                        ),

                    toi =
                        obj.optNullableString(
                            "toi"
                        ),

                    decision =
                        obj.optNullableString(
                            "decision"
                        )
                )
            )
        }


        return result
    }

    suspend fun getStandings(): NHLStandings {

        val json =
            getJson(
                "$API_BASE/standings/now"
            )

        return parseStandings(
            json
        )
    }

    private fun parseStandings(
        obj: JSONObject
    ): NHLStandings {

        val array =
            obj.optJSONArray(
                "standings"
            )


        if (array == null) {

            return NHLStandings(
                date =
                    obj.optNullableString(
                        "standingsDate"
                    ),

                teams =
                    emptyList()
            )
        }


        val teams =
            mutableListOf<NHLStandingsTeam>()


        for (
        i in 0 until array.length()
        ) {

            val team =
                array.optJSONObject(i)
                    ?: continue


            teams.add(
                parseStandingsTeam(
                    team
                )
            )
        }


        return NHLStandings(
            date =
                obj.optNullableString(
                    "standingsDate"
                ),

            teams =
                teams
        )
    }

    private fun parseStandingsTeam(
        obj: JSONObject
    ): NHLStandingsTeam {

        val abbrev =
            obj.optJSONObject(
                "teamAbbrev"
            )
                ?.optNullableLocalizedString()
                ?: "?"


        val name =
            obj.optJSONObject(
                "teamName"
            )
                ?.optNullableLocalizedString()
                ?: abbrev


        val goalsFor =
            obj.optNullableInt(
                "goalFor"
            )
                ?: obj.optNullableInt(
                    "goalsFor"
                )


        val goalsAgainst =
            obj.optNullableInt(
                "goalAgainst"
            )
                ?: obj.optNullableInt(
                    "goalsAgainst"
                )


        val goalDifferential =
            obj.optNullableInt(
                "goalDifferential"
            )
                ?: if (
                    goalsFor != null &&
                    goalsAgainst != null
                ) {
                    goalsFor -
                            goalsAgainst
                } else {
                    null
                }


        return NHLStandingsTeam(
            teamAbbrev =
                abbrev,

            teamName =
                name,

            teamLogo =
                obj.optNullableString(
                    "teamLogo"
                ),

            conferenceName =
                obj.optNullableString(
                    "conferenceName"
                ),

            divisionName =
                obj.optNullableString(
                    "divisionName"
                ),

            gamesPlayed =
                obj.optInt(
                    "gamesPlayed",
                    0
                ),

            wins =
                obj.optInt(
                    "wins",
                    0
                ),

            losses =
                obj.optInt(
                    "losses",
                    0
                ),

            otLosses =
                obj.optInt(
                    "otLosses",
                    0
                ),

            points =
                obj.optInt(
                    "points",
                    0
                ),

            pointPctg =
                obj.optNullableDouble(
                    "pointPctg"
                ),

            goalsFor =
                goalsFor,

            goalsAgainst =
                goalsAgainst,

            goalDifferential =
                goalDifferential,

            leagueSequence =
                obj.optNullableInt(
                    "leagueSequence"
                ),

            conferenceSequence =
                obj.optNullableInt(
                    "conferenceSequence"
                ),

            divisionSequence =
                obj.optNullableInt(
                    "divisionSequence"
                ),

            wildcardSequence =
                obj.optNullableInt(
                    "wildcardSequence"
                ),

            streakCode =
                obj.optNullableString(
                    "streakCode"
                ),

            streakCount =
                obj.optNullableInt(
                    "streakCount"
                )
        )
    }

    suspend fun getPlayerProfile(
        playerId: Long?
    ): NHLPlayerProfile? {

        if (playerId == null) {
            return null
        }


        return try {

            val obj =
                getJson(
                    "$API_BASE/player/$playerId/landing"
                )


            val seasonStats =
                mutableListOf<NHLSeasonStats>()


            val totals =
                obj.optJSONArray(
                    "seasonTotals"
                )


            if (totals != null) {

                for (
                i in 0 until totals.length()
                ) {

                    val season =
                        totals.getJSONObject(i)


                    val teamName =
                        season
                            .optJSONObject(
                                "teamName"
                            )
                            ?.optNullableLocalizedString()
                            ?: ""


                    seasonStats.add(
                        NHLSeasonStats(
                            season =
                                season.optInt(
                                    "season"
                                ),

                            teamName =
                                teamName,

                            teamAbbrev =
                                teamNameToAbbrev(
                                    teamName
                                ),

                            league =
                                season.optString(
                                    "leagueAbbrev"
                                ),

                            gameTypeId =
                                season.optInt(
                                    "gameTypeId"
                                ),

                            gamesPlayed =
                                season.optInt(
                                    "gamesPlayed"
                                ),

                            goals =
                                season.optInt(
                                    "goals"
                                ),

                            assists =
                                season.optInt(
                                    "assists"
                                ),

                            points =
                                season.optInt(
                                    "points"
                                ),

                            plusMinus =
                                season.optInt(
                                    "plusMinus"
                                ),

                            pim =
                                season.optInt(
                                    "pim"
                                ),

                            wins =
                                season.optInt(
                                    "wins"
                                ),

                            losses =
                                season.optInt(
                                    "losses"
                                ),

                            otLosses =
                                season.optInt(
                                    "otLosses"
                                ),

                            shutouts =
                                season.optInt(
                                    "shutouts"
                                ),

                            savePctg =
                                if (
                                    season.has(
                                        "savePctg"
                                    )
                                ) {

                                    season.optDouble(
                                        "savePctg"
                                    )

                                } else {

                                    null
                                },

                            goalsAgainstAverage =
                                if (
                                    season.has(
                                        "goalsAgainstAvg"
                                    )
                                ) {

                                    season.optDouble(
                                        "goalsAgainstAvg"
                                    )

                                } else {

                                    null
                                }
                        )
                    )
                }
            }


            NHLPlayerProfile(
                playerId =
                    obj.getLong(
                        "playerId"
                    ),

                firstName =
                    obj
                        .optJSONObject(
                            "firstName"
                        )
                        ?.optNullableLocalizedString()
                        ?: "",

                lastName =
                    obj
                        .optJSONObject(
                            "lastName"
                        )
                        ?.optNullableLocalizedString()
                        ?: "",

                isActive =
                    obj.optBoolean(
                        "isActive",
                        false
                    ),

                inHallOfFame =
                    obj.optInt(
                        "inHHOF",
                        0
                    ) == 1,

                currentTeamAbbrev =
                    obj.optNullableString(
                        "currentTeamAbbrev"
                    ),

                teamLogo =
                    obj.optNullableString(
                        "teamLogo"
                    ),

                headshot =
                    obj.optNullableString(
                        "headshot"
                    ),

                position =
                    obj.optNullableString(
                        "position"
                    ),

                sweaterNumber =
                    obj.optNullableInt(
                        "sweaterNumber"
                    ),

                seasonStats =
                    seasonStats
            )

        } catch (
            e: Exception
        ) {

            e.printStackTrace()

            null
        }
    }

    suspend fun getSkaterLeaders(
        category: String,
        limit: Int = 10,
        season: Int? = null,
        gameType: Int? = null
    ): List<NHLStatLeader> {

        val path =
            if (
                season != null &&
                gameType != null
            ) {

                "$API_BASE/skater-stats-leaders/" +
                        "$season/$gameType"

            } else {

                "$API_BASE/skater-stats-leaders/current"
            }


        val json =
            getJson(
                "$path" +
                        "?categories=$category" +
                        "&limit=$limit"
            )


        return parseLeaderCategory(
            root =
                json,

            category =
                category
        )
    }

    suspend fun getGoalieLeaders(
        category: String,
        limit: Int = 10,
        season: Int? = null,
        gameType: Int? = null
    ): List<NHLStatLeader> {

        val path =
            if (
                season != null &&
                gameType != null
            ) {

                "$API_BASE/goalie-stats-leaders/" +
                        "$season/$gameType"

            } else {

                "$API_BASE/goalie-stats-leaders/current"
            }


        val json =
            getJson(
                "$path" +
                        "?categories=$category" +
                        "&limit=$limit"
            )


        return parseLeaderCategory(
            root =
                json,

            category =
                category
        )
    }

    suspend fun getPlayoffBracket(
        year: Int
    ): NHLPlayoffBracket {

        val json =
            getJson(
                "$API_BASE/playoff-bracket/$year"
            )

        return parsePlayoffBracket(
            year = year,
            obj = json
        )
    }

    private fun parsePlayoffBracket(
        year: Int,
        obj: JSONObject
    ): NHLPlayoffBracket {

        val seriesArray =
            obj.optJSONArray(
                "series"
            )


        val series =
            mutableListOf<NHLPlayoffSeries>()


        if (seriesArray != null) {

            for (
            i in 0 until seriesArray.length()
            ) {

                val item =
                    seriesArray.optJSONObject(i)
                        ?: continue


                series.add(
                    parsePlayoffSeries(
                        item
                    )
                )
            }
        }


        return NHLPlayoffBracket(
            year = year,

            bracketLogo =
                obj.optNullableString(
                    "bracketLogo"
                ),

            series =
                series
        )
    }

    private fun parsePlayoffSeries(
        obj: JSONObject
    ): NHLPlayoffSeries {

        return NHLPlayoffSeries(

            seriesLetter =
                obj.optNullableString(
                    "seriesLetter"
                )
                    ?: "",


            seriesTitle =
                obj.optNullableString(
                    "seriesTitle"
                )
                    ?: "Playoff Series",


            seriesAbbrev =
                obj.optNullableString(
                    "seriesAbbrev"
                ),


            playoffRound =
                obj.optInt(
                    "playoffRound",
                    0
                ),


            conferenceAbbrev =
                obj.optNullableString(
                    "conferenceAbbrev"
                ),


            conferenceName =
                obj.optNullableString(
                    "conferenceName"
                ),


            topSeedRank =
                obj.optNullableInt(
                    "topSeedRank"
                ),


            topSeedRankAbbrev =
                obj.optNullableString(
                    "topSeedRankAbbrev"
                ),


            topSeedWins =
                obj.optInt(
                    "topSeedWins",
                    0
                ),


            bottomSeedRank =
                obj.optNullableInt(
                    "bottomSeedRank"
                ),


            bottomSeedRankAbbrev =
                obj.optNullableString(
                    "bottomSeedRankAbbrev"
                ),


            bottomSeedWins =
                obj.optInt(
                    "bottomSeedWins",
                    0
                ),


            winningTeamId =
                obj.optLong(
                    "winningTeamId"
                )
                    .takeIf {
                        it > 0
                    },


            losingTeamId =
                obj.optLong(
                    "losingTeamId"
                )
                    .takeIf {
                        it > 0
                    },


            topSeedTeam =
                parsePlayoffTeam(
                    obj.optJSONObject(
                        "topSeedTeam"
                    )
                ),


            bottomSeedTeam =
                parsePlayoffTeam(
                    obj.optJSONObject(
                        "bottomSeedTeam"
                    )
                )
        )
    }

    private fun parsePlayoffTeam(
        obj: JSONObject?
    ): NHLPlayoffTeam? {

        if (obj == null) {
            return null
        }


        val id =
            obj.optLong(
                "id"
            )


        if (id <= 0) {
            return null
        }


        return NHLPlayoffTeam(

            id =
                id,


            abbrev =
                obj.optNullableString(
                    "abbrev"
                )
                    ?: "?",


            name =
                obj
                    .optJSONObject(
                        "name"
                    )
                    ?.optNullableLocalizedString()
                    ?: obj.optNullableString(
                        "abbrev"
                    )
                    ?: "Unknown Team",


            commonName =
                obj
                    .optJSONObject(
                        "commonName"
                    )
                    ?.optNullableLocalizedString(),


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

    suspend fun getPlayoffSeriesSchedule(
        season: Int,
        seriesLetter: String
    ): NHLPlayoffSeriesDetail {

        val json =
            getJson(
                "$API_BASE/schedule/playoff-series/" +
                        "$season/${seriesLetter.lowercase()}"
            )


        return parsePlayoffSeriesSchedule(
            season = season,
            seriesLetter = seriesLetter,
            obj = json
        )
    }

    private fun parsePlayoffSeriesSchedule(
        season: Int,
        seriesLetter: String,
        obj: JSONObject
    ): NHLPlayoffSeriesDetail {

        val games =
            mutableListOf<NHLGame>()


        /*
         * NHL schedule responses commonly contain:
         *
         * gameWeek [
         *     {
         *         date: ...,
         *         games: [...]
         *     }
         * ]
         *
         * Some series responses may also expose games directly,
         * so support both layouts.
         */

        val gameWeek =
            obj.optJSONArray(
                "gameWeek"
            )


        if (gameWeek != null) {

            for (
            weekIndex in 0 until gameWeek.length()
            ) {

                val day =
                    gameWeek.optJSONObject(
                        weekIndex
                    )
                        ?: continue


                val dayGames =
                    day.optJSONArray(
                        "games"
                    )
                        ?: continue


                for (
                gameIndex in 0 until dayGames.length()
                ) {

                    val game =
                        dayGames.optJSONObject(
                            gameIndex
                        )
                            ?: continue


                    games.add(
                        parseGame(
                            game
                        )
                    )
                }
            }
        }


        val directGames =
            obj.optJSONArray(
                "games"
            )


        if (
            games.isEmpty() &&
            directGames != null
        ) {

            for (
            index in 0 until directGames.length()
            ) {

                val game =
                    directGames.optJSONObject(
                        index
                    )
                        ?: continue


                games.add(
                    parseGame(
                        game
                    )
                )
            }
        }


        return NHLPlayoffSeriesDetail(
            season = season,

            seriesLetter =
                seriesLetter.uppercase(),

            games =
                games
                    .distinctBy {
                        it.id
                    }
                    .sortedWith(
                        compareBy<NHLGame> {
                            it.gameDate
                        }
                            .thenBy {
                                it.startTimeUTC
                            }
                    )
        )
    }

    suspend fun getTeamSeasonSchedule(
        teamAbbrev: String,
        season: Int
    ): List<NHLGame> {

        val json =
            getJson(
                "$API_BASE/club-schedule-season/" +
                        "${teamAbbrev.uppercase()}/$season"
            )


        return parseTeamSeasonSchedule(
            json
        )
    }

    private fun parseTeamSeasonSchedule(
        obj: JSONObject
    ): List<NHLGame> {

        val result =
            mutableListOf<NHLGame>()


        val games =
            obj.optJSONArray(
                "games"
            )
                ?: return emptyList()


        for (
        index in 0 until games.length()
        ) {

            val game =
                games.optJSONObject(
                    index
                )
                    ?: continue


            result.add(
                parseGame(
                    game
                )
            )
        }


        return result
    }

    suspend fun getProjectionStatsForTeams(
        teamAbbrevs: Collection<String>,
        season: Int,
        standings: List<NHLStandingsTeam>
    ): Map<String, TeamProjectionStats> {

        val result =
            mutableMapOf<String, TeamProjectionStats>()


        teamAbbrevs
            .distinct()
            .forEach {
                    abbrev ->

                val standing =
                    standings
                        .firstOrNull {

                            it.teamAbbrev
                                .equals(
                                    abbrev,
                                    ignoreCase = true
                                )
                        }
                        ?: return@forEach


                val schedule =
                    getTeamSeasonSchedule(
                        teamAbbrev =
                            abbrev,

                        season =
                            season
                    )


                result[
                    abbrev.uppercase()
                ] =
                    buildTeamProjectionStats(
                        standings =
                            standing,

                        seasonGames =
                            schedule
                    )
            }


        return result
    }

    private fun parseLeaderCategory(
        root: JSONObject,
        category: String
    ): List<NHLStatLeader> {

        val array =
            root.optJSONArray(
                category
            )
                ?: return emptyList()


        val result =
            mutableListOf<NHLStatLeader>()


        for (
        i in 0 until array.length()
        ) {

            val obj =
                array.optJSONObject(i)
                    ?: continue


            result.add(
                parseLeader(
                    obj
                )
            )
        }


        return result
    }

    private fun parseLeader(
        obj: JSONObject
    ): NHLStatLeader {

        return NHLStatLeader(

            playerId =
                obj.optLong(
                    "id"
                )
                    .takeIf {
                        it > 0
                    }
                    ?: obj.optLong(
                        "playerId"
                    )
                        .takeIf {
                            it > 0
                        },


            firstName =
                obj
                    .optJSONObject(
                        "firstName"
                    )
                    ?.optNullableLocalizedString(),


            lastName =
                obj
                    .optJSONObject(
                        "lastName"
                    )
                    ?.optNullableLocalizedString(),


            teamAbbrev =
                obj
                    .optJSONObject(
                        "teamAbbrev"
                    )
                    ?.optNullableLocalizedString()
                    ?: obj.optNullableString(
                        "teamAbbrev"
                    ),


            teamName =
                obj
                    .optJSONObject(
                        "teamName"
                    )
                    ?.optNullableLocalizedString(),


            headshot =
                obj.optNullableString(
                    "headshot"
                ),


            teamLogo =
                obj.optNullableString(
                    "teamLogo"
                ),


            position =
                obj.optNullableString(
                    "position"
                ),


            sweaterNumber =
                obj.optNullableInt(
                    "sweaterNumber"
                ),


            value =
                obj.optNullableDouble(
                    "value"
                )
        )
    }

    suspend fun getSchedule(
        date: LocalDate
    ): List<NHLGame> {

        val json =
            getJson(
                "$API_BASE/schedule/$date"
            )

        return parseSchedule(
            json
        )
    }

    private fun parseSchedule(
        obj: JSONObject
    ): List<NHLGame> {

        val result =
            mutableListOf<NHLGame>()

        val gameWeek =
            obj.optJSONArray(
                "gameWeek"
            )
                ?: return emptyList()

        for (
        dayIndex in 0 until
                gameWeek.length()
        ) {

            val day =
                gameWeek.optJSONObject(
                    dayIndex
                )
                    ?: continue

            val games =
                day.optJSONArray(
                    "games"
                )
                    ?: continue

            for (
            gameIndex in 0 until
                    games.length()
            ) {

                val game =
                    games.optJSONObject(
                        gameIndex
                    )
                        ?: continue

                result.add(
                    parseGame(
                        game
                    )
                )
            }
        }

        return result
            .distinctBy {
                it.id
            }
            .sortedWith(
                compareBy<NHLGame> {
                    it.gameDate
                }
                    .thenBy {
                        it.startTimeUTC
                    }
            )
    }
}

private fun JSONObject.optNullableDouble(
    name: String
): Double? {

    if (
        !has(name) ||
        isNull(name)
    ) {
        return null
    }

    return optDouble(name)
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

    return optInt(name)
}

private fun JSONObject.optNullableLocalizedString(): String? {

    return optNullableString(
        "default"
    )
        ?: optNullableString(
            "fr"
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

    return optString(name)
        .trim()
        .takeIf {
            it.isNotBlank()
        }
}