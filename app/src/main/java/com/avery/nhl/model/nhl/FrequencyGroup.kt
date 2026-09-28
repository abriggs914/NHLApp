package com.avery.nhl.model.nhl

data class FrequencyGroup(
    val title: String,
    val mostCommon: List<FrequencyItem>,
    val leastCommon: List<FrequencyItem>
)