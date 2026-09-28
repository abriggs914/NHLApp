package com.avery.nhl.model.jersey

data class CollectionFilters(
    val cancelled: Set<String> = emptySet(),
    val leagues: Set<String> = emptySet(),
    val teams: Set<String> = emptySet(),
    val players: Set<String> = emptySet(),
    val sizes: Set<String> = emptySet(),
    val brands: Set<String> = emptySet(),
    val makes: Set<String> = emptySet(),
    val models: Set<String> = emptySet(),
    val numbers: Set<Int> = emptySet(),
    val suppliers: Set<String> = emptySet(),
    val colours: Set<String> = emptySet(),

    val aPatches: Set<String> = emptySet(),
    val cPatches: Set<String> = emptySet(),

    val nationalities: Set<String> = emptySet(),
    val positions: Set<String> = emptySet(),
    val firstSeasons: Set<String> = emptySet(),
    val lastSeasons: Set<String> = emptySet(),

    val jerseyType: JerseyType = JerseyType.ALL,
    val imageFilter: ImageFilter = ImageFilter.ALL,

    val dob: MonthYearFilter = MonthYearFilter(),
    val orderDate: MonthYearFilter = MonthYearFilter(),
    val receiveDate: MonthYearFilter = MonthYearFilter(),
    val openDate: MonthYearFilter = MonthYearFilter(),
    val manufactureDate: MonthYearFilter = MonthYearFilter()
)