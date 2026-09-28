package com.avery.nhl.model.nhl

data class NHLSeasonStats(
    val season: Int,
    val teamName: String,
    val teamAbbrev: String?,
    val league: String,
    val gameTypeId: Int,

    val gamesPlayed: Int = 0,

    // Skaters
    val goals: Int = 0,
    val assists: Int = 0,
    val points: Int = 0,
    val plusMinus: Int = 0,
    val pim: Int = 0,

    // Goalies
    val wins: Int = 0,
    val losses: Int = 0,
    val otLosses: Int = 0,
    val shutouts: Int = 0,
    val savePctg: Double? = null,
    val goalsAgainstAverage: Double? = null
)