package com.avery.nhl.model.nhl

data class NHLTeamStats(
    val teamAbbrev: String?,
    val teamName: String,
    val season: NHLSeasonStats,

    val gamesPlayed: Int,

    val goals: Int,
    val assists: Int,
    val points: Int,
    val plusMinus: Int,
    val pim: Int,

    val wins: Int,
    val losses: Int,
    val otLosses: Int,
    val shutouts: Int
)