package com.avery.nhl.model.nhl


data class NHLPlayoffSeriesDetail(
    val season: Int,
    val seriesLetter: String,
    val games: List<NHLGame>
)