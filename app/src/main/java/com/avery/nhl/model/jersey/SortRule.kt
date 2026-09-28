package com.avery.nhl.model.jersey

data class SortRule(
    val field: JerseySort,
    val ascending: Boolean = true
)