package com.avery.nhl.model.jersey

import com.avery.nhl.model.nhl.AnniversaryType
import com.avery.nhl.model.nhl.CollectionAnniversary
import com.avery.nhl.model.nhl.FrequencyGroup
import com.avery.nhl.model.nhl.FrequencyItem
import com.avery.nhl.model.nhl.ManufactureMonth
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.random.Random


data class SimpleDateParts(
    val year: Int,
    val month: Int
)


fun filterJerseys(
    jerseys: List<Jersey>,
    filters: CollectionFilters
): List<Jersey> {

    return jerseys.filter { jersey ->

        val cancelledMatches =
            filters.cancelled.isEmpty() ||
                    jersey.cancelled in filters.cancelled

        val leagueMatches =
            filters.leagues.isEmpty() ||
                    jersey.league in filters.leagues

        val teamMatches =
            filters.teams.isEmpty() ||
                    jersey.team in filters.teams

        val playerMatches =
            filters.players.isEmpty() ||
                    jersey.playerName in filters.players

        val sizeMatches =
            filters.sizes.isEmpty() ||
                    jersey.size in filters.sizes

        val brandMatches =
            filters.brands.isEmpty() ||
                    jersey.brand in filters.brands

        val makeMatches =
            filters.makes.isEmpty() ||
                    jersey.make in filters.makes

        val modelMatches =
            filters.models.isEmpty() ||
                    jersey.model in filters.models

        val numberMatches =
            filters.numbers.isEmpty() ||
                    jersey.number in filters.numbers

        val supplierMatches =
            filters.suppliers.isEmpty() ||
                    jersey.supplier in filters.suppliers

        val colourMatches =
            filters.colours.isEmpty() ||
                    jersey.colours.any { jerseyColour ->

                        filters.colours.any { selectedColour ->

                            jerseyColour.equals(
                                selectedColour,
                                ignoreCase = true
                            )
                        }
                    }

        val nationalityMatches =
            filters.nationalities.isEmpty() ||
                    jersey.nationality in filters.nationalities

        val positionMatches =
            filters.positions.isEmpty() ||
                    jersey.position in filters.positions

        val firstSeasonMatches =
            filters.firstSeasons.isEmpty() ||
                    jersey.firstSeason in filters.firstSeasons

        val lastSeasonMatches =
            filters.lastSeasons.isEmpty() ||
                    jersey.lastSeason in filters.lastSeasons

        val cPatchMatches =
            filters.cPatches.isEmpty() ||
                    jersey.cPatch in filters.cPatches

        val aPatchMatches =
            filters.aPatches.isEmpty() ||
                    jersey.aPatch in filters.aPatches


        val jerseyTypeMatches =
            when (filters.jerseyType) {

                JerseyType.ALL ->
                    true

                JerseyType.PLAYER ->
                    jersey.hasPlayer

                JerseyType.BLANK ->
                    !jersey.hasPlayer
            }


        val imageMatches =
            when (filters.imageFilter) {

                ImageFilter.ALL ->
                    true

                ImageFilter.HAS_IMAGES ->
                    jersey.images.isNotEmpty()

                ImageFilter.NO_IMAGES ->
                    jersey.images.isEmpty()
            }


        val dobMatches =
            matchesMonthYear(
                dateValue =
                    jersey.details["DOB"],

                filter =
                    filters.dob
            )


        val orderDateMatches =
            matchesMonthYear(
                dateValue =
                    jersey.orderDate,

                filter =
                    filters.orderDate
            )


        val receiveDateMatches =
            matchesMonthYear(
                dateValue =
                    jersey.receiveDate,

                filter =
                    filters.receiveDate
            )


        val openDateMatches =
            matchesMonthYear(
                dateValue =
                    jersey.openDate,

                filter =
                    filters.openDate
            )


        val manufactureDateMatches =
            matchesMonthYear(
                dateValue =
                    jersey.manufactureDate,

                filter =
                    filters.manufactureDate
            )


        cancelledMatches &&
                leagueMatches &&
                teamMatches &&
                playerMatches &&
                sizeMatches &&
                brandMatches &&
                makeMatches &&
                modelMatches &&
                numberMatches &&
                supplierMatches &&
                colourMatches &&
                nationalityMatches &&
                positionMatches &&
                firstSeasonMatches &&
                lastSeasonMatches &&
                cPatchMatches &&
                aPatchMatches &&
                jerseyTypeMatches &&
                imageMatches &&
                dobMatches &&
                orderDateMatches &&
                receiveDateMatches &&
                openDateMatches &&
                manufactureDateMatches
    }
}

fun sortJerseys(
    jerseys: List<Jersey>,
    sortRules: List<SortRule>,
    sortMode: SortMode,
    randomSeed: Int = 0
): List<Jersey> {

    if (
        sortMode ==
        SortMode.RANDOM
    ) {

        return jerseys.shuffled(
            Random(
                randomSeed
            )
        )
    }


    if (
        sortRules.isEmpty()
    ) {
        return jerseys
    }


    val comparator =
        Comparator<Jersey> { a, b ->

            for (
            rule in sortRules
            ) {

                var result =
                    compareJerseysByField(
                        a = a,
                        b = b,
                        field = rule.field
                    )


                if (
                    !rule.ascending
                ) {
                    result = -result
                }


                if (
                    result != 0
                ) {
                    return@Comparator result
                }
            }


            // Stable final tie-breaker.
            a.id.compareTo(
                b.id
            )
        }


    return jerseys.sortedWith(
        comparator
    )
}

private fun compareJerseysByField(
    a: Jersey,
    b: Jersey,
    field: JerseySort
): Int {

    return when (field) {

        JerseySort.ID ->
            a.id.compareTo(
                b.id
            )


        JerseySort.LEAGUE ->
            compareNullableText(
                a.league,
                b.league
            )


        JerseySort.TEAM ->
            compareNullableText(
                a.team,
                b.team
            )


        JerseySort.PLAYER ->
            compareNullableText(
                a.playerName,
                b.playerName
            )


        JerseySort.NUMBER ->
            compareNullableInt(
                a.number,
                b.number
            )


        JerseySort.BRAND ->
            compareNullableText(
                a.brand,
                b.brand
            )


        JerseySort.MAKE ->
            compareNullableText(
                a.make,
                b.make
            )


        JerseySort.MODEL ->
            compareNullableText(
                a.model,
                b.model
            )


        JerseySort.SUPPLIER ->
            compareNullableText(
                a.supplier,
                b.supplier
            )


        JerseySort.SIZE ->
            compareNullableText(
                a.size,
                b.size
            )


        JerseySort.COLOUR1 ->
            compareNullableText(
                a.colour1,
                b.colour1
            )


        JerseySort.COLOUR2 ->
            compareNullableText(
                a.colour2,
                b.colour2
            )


        JerseySort.COLOUR3 ->
            compareNullableText(
                a.colour3,
                b.colour3
            )


        JerseySort.COLOURS ->
            compareNullableText(
                a.colours.joinToString("|"),
                b.colours.joinToString("|")
            )


        JerseySort.DOB ->
            compareNullableText(
                a.details["DOB"],
                b.details["DOB"]
            )


        JerseySort.ORDER_DATE ->
            compareNullableText(
                a.orderDate,
                b.orderDate
            )


        JerseySort.RECEIVE_DATE ->
            compareNullableText(
                a.receiveDate,
                b.receiveDate
            )


        JerseySort.OPEN_DATE ->
            compareNullableText(
                a.openDate,
                b.openDate
            )


        JerseySort.MANUFACTURE_DATE ->
            compareNullableText(
                a.manufactureDate,
                b.manufactureDate
            )


        JerseySort.NATIONALITY ->
            compareNullableText(
                a.nationality,
                b.nationality
            )


        JerseySort.POSITION ->
            compareNullableText(
                a.position,
                b.position
            )


        JerseySort.APATCH ->
            compareNullableText(
                a.aPatch,
                b.aPatch
            )


        JerseySort.CPATCH ->
            compareNullableText(
                a.cPatch,
                b.cPatch
            )


        JerseySort.FIRSTSEASON ->
            compareNullableText(
                a.firstSeason,
                b.firstSeason
            )


        JerseySort.LASTSEASON ->
            compareNullableText(
                a.lastSeason,
                b.lastSeason
            )


        JerseySort.IMAGE_COUNT ->
            a.images.size.compareTo(
                b.images.size
            )
    }
}

fun extractDateParts(
    value: String?
): SimpleDateParts? {

    if (
        value.isNullOrBlank() ||
        value.length < 7
    ) {
        return null
    }

    val year =
        value.substring(
            0,
            4
        )
            .toIntOrNull()
            ?: return null


    val month =
        value.substring(
            5,
            7
        )
            .toIntOrNull()
            ?: return null


    if (
        month !in 1..12
    ) {
        return null
    }


    return SimpleDateParts(
        year = year,
        month = month
    )
}


fun matchesMonthYear(
    dateValue: String?,
    filter: MonthYearFilter
): Boolean {

    if (
        filter.month == null &&
        filter.year == null
    ) {
        return true
    }


    val parts =
        extractDateParts(
            dateValue
        )
            ?: return false


    val monthMatches =
        filter.month == null ||
                parts.month == filter.month


    val yearMatches =
        filter.year == null ||
                parts.year == filter.year


    return monthMatches &&
            yearMatches
}

private fun compareNullableText(
    a: String?,
    b: String?
): Int {

    if (
        a == null &&
        b == null
    ) {
        return 0
    }

    if (
        a == null
    ) {
        return 1
    }

    if (
        b == null
    ) {
        return -1
    }

    return a.compareTo(
        b,
        ignoreCase = true
    )
}


private fun compareNullableInt(
    a: Int?,
    b: Int?
): Int {

    if (
        a == null &&
        b == null
    ) {
        return 0
    }

    if (
        a == null
    ) {
        return 1
    }

    if (
        b == null
    ) {
        return -1
    }

    return a.compareTo(
        b
    )
}

fun parseCollectionDate(
    value: String?
): LocalDate? {

    if (value.isNullOrBlank()) {
        return null
    }

    return try {

        LocalDate.parse(
            value.take(10)
        )

    } catch (_: Exception) {

        null
    }
}


fun anniversaryForYear(
    original: LocalDate,
    year: Int
): LocalDate? {

    return try {

        original.withYear(year)

    } catch (_: Exception) {

        if (
            original.monthValue == 2 &&
            original.dayOfMonth == 29
        ) {

            LocalDate.of(
                year,
                2,
                28
            )

        } else {

            null
        }
    }
}


fun nearestAnniversary(
    originalDate: LocalDate,
    today: LocalDate
): Pair<LocalDate, Long>? {

    val candidates =
        listOf(
            today.year - 1,
            today.year,
            today.year + 1
        )
            .mapNotNull {
                anniversaryForYear(
                    originalDate,
                    it
                )
            }


    val closest =
        candidates.minByOrNull {

            kotlin.math.abs(
                ChronoUnit.DAYS.between(
                    today,
                    it
                )
            )
        }
            ?: return null


    return closest to
            ChronoUnit.DAYS.between(
                today,
                closest
            )
}

fun parseManufactureDate(
    value: String?
): ManufactureMonth? {

    val cleaned =
        value
            ?.trim()
            ?.takeIf {
                it.isNotBlank() &&
                        it != "-"
            }
            ?: return null


    val parts =
        cleaned.split("/")


    if (parts.size != 2) {
        return null
    }


    val month =
        parts[0].toIntOrNull()
            ?: return null


    var year =
        parts[1].toIntOrNull()
            ?: return null


    if (month !in 1..12) {
        return null
    }


    if (year < 100) {
        year += 2000
    }


    return ManufactureMonth(
        month = month,
        year = year
    )
}

fun manufactureAnniversary(
    jersey: Jersey,
    today: LocalDate
): CollectionAnniversary? {

    val manufacture =
        parseManufactureDate(
            jersey.details[
                "ManufactureDate"
            ]
        )
            ?: return null


    val years =
        today.year -
                manufacture.year


    if (years < 1) {
        return null
    }


    val thisYear =
        LocalDate.of(
            today.year,
            manufacture.month,
            1
        )


    val occurrence =
        when {

            thisYear.isBefore(
                today.withDayOfMonth(1)
            ) ->

                LocalDate.of(
                    today.year + 1,
                    manufacture.month,
                    1
                )


            else ->
                thisYear
        }


    val actualYears =
        occurrence.year -
                manufacture.year


    val daysAway =
        ChronoUnit.DAYS.between(
            today,
            occurrence
        )


    return CollectionAnniversary(
        type =
            AnniversaryType.MANUFACTURE,

        jersey =
            jersey,

        title =
            jersey.toDisplayString(),

        originalDate =
            LocalDate.of(
                manufacture.year,
                manufacture.month,
                1
            ),

        occurrenceDate =
            occurrence,

        years =
            actualYears,

        daysAway =
            daysAway
    )
}

fun buildCollectionAnniversaries(
    jerseys: List<Jersey>,
    today: LocalDate =
        LocalDate.now()
): List<CollectionAnniversary> {

    val active =
        jerseys.filter {

            !it.cancelled.equals(
                "true",
                ignoreCase = true
            )
        }


    val results =
        mutableListOf<CollectionAnniversary>()


    fun addJerseyDate(
        jersey: Jersey,
        type: AnniversaryType,
        value: String?
    ) {

        val original =
            parseCollectionDate(
                value
            )
                ?: return


        val nearest =
            nearestAnniversary(
                original,
                today
            )
                ?: return


        val occurrence =
            nearest.first


        val daysAway =
            nearest.second


        val years =
            occurrence.year -
                    original.year


        if (years < 1) {
            return
        }


        results.add(
            CollectionAnniversary(
                type =
                    type,

                jersey =
                    jersey,

                title =
                    jersey.toDisplayString(),

                originalDate =
                    original,

                occurrenceDate =
                    occurrence,

                years =
                    years,

                daysAway =
                    daysAway
            )
        )
    }


    active.forEach {
            jersey ->

        addJerseyDate(
            jersey,
            AnniversaryType.ORDER,
            jersey.orderDate
        )


        addJerseyDate(
            jersey,
            AnniversaryType.RECEIVE,
            jersey.receiveDate
        )


        addJerseyDate(
            jersey,
            AnniversaryType.OPEN,
            jersey.openDate
        )


//        addJerseyDate(
//            jersey,
//            AnniversaryType.MANUFACTURE,
//            jersey.manufactureDate
//        )
    }


    // One birthday entry per player.
    active
        .filter {
            !it.playerName.isNullOrBlank()
        }
        .distinctBy {

            it.nhlId
                ?: it.playerName
                    ?.lowercase()
        }
        .forEach {
                jersey ->

            val dob =
                parseCollectionDate(
                    jersey.details["DOB"]
                )
                    ?: return@forEach


            val nearest =
                nearestAnniversary(
                    dob,
                    today
                )
                    ?: return@forEach


            val occurrence =
                nearest.first


            val age =
                occurrence.year -
                        dob.year


            results.add(
                CollectionAnniversary(
                    type =
                        AnniversaryType.DOB,

                    jersey =
                        jersey,

                    title =
                        jersey.playerName
                            ?: jersey.team,

                    originalDate =
                        dob,

                    occurrenceDate =
                        occurrence,

                    years =
                        age,

                    daysAway =
                        nearest.second
                )
            )
        }


    active.forEach {
            jersey ->

        val manufacture =
            manufactureAnniversary(
                jersey =
                    jersey,

                today =
                    today
            )


        if (manufacture != null) {

            results.add(
                manufacture
            )
        }
    }


    return results
}

fun formatDays(
    value: Double?
): String {

    return value
        ?.let {

            String.format(
                Locale.CANADA,
                "%.1f days",
                it
            )
        }
        ?: "-"
}

fun frequencyItems(
    values: List<String?>
): List<FrequencyItem> {

    return values
        .mapNotNull {
                value ->

            value
                ?.trim()
                ?.takeIf {
                    it.isNotBlank() &&
                            it != "-"
                }
        }
        .groupingBy {
            it
        }
        .eachCount()
        .map {
                (label, count) ->

            FrequencyItem(
                label = label,
                count = count
            )
        }
}

fun frequencyGroup(
    title: String,
    values: List<String?>
): FrequencyGroup {

    val items =
        frequencyItems(
            values
        )


    val most =
        items
            .sortedWith(
                compareByDescending<FrequencyItem> {
                    it.count
                }
                    .thenBy {
                        it.label.lowercase()
                    }
            )
            .take(5)


    val least =
        items
            .sortedWith(
                compareBy<FrequencyItem> {
                    it.count
                }
                    .thenBy {
                        it.label.lowercase()
                    }
            )
            .take(5)


    return FrequencyGroup(
        title =
            title,

        mostCommon =
            most,

        leastCommon =
            least
    )
}

fun dateMonthLabel(
    value: String?
): String? {

    val date =
        parseCollectionDate(
            value
        )
            ?: return null


    return date.month
        .getDisplayName(
            java.time.format.TextStyle.SHORT,
            Locale.CANADA
        )
}


fun dateYearLabel(
    value: String?
): String? {

    return parseCollectionDate(
        value
    )
        ?.year
        ?.toString()
}

fun priceBucket(
    value: Double?,
    increment: Int = 10
): String? {

    if (value == null) {
        return null
    }


    val lower =
        kotlin.math.floor(
            value / increment
        )
            .toInt() * increment


    val upper =
        lower + increment


    return "$$lower–$$upper"
}


fun priceFrequencyGroup(
    title: String,
    values: List<Double?>
): FrequencyGroup {

    return frequencyGroup(
        title =
            title,

        values =
            values.map {
                priceBucket(
                    it
                )
            }
    )
}

fun buildFrequencyGroups(
    jerseys: List<Jersey>
): List<FrequencyGroup> {

    val active =
        jerseys.filter {

            !it.cancelled.equals(
                "true",
                ignoreCase = true
            )
        }


    return listOf(

        frequencyGroup(
            "Team",
            active.map {
                it.team
            }
        ),

        frequencyGroup(
            "League",
            active.map {
                it.league
            }
        ),

        frequencyGroup(
            "Conference",
            active.map {
                it.details["Conference"]
            }
        ),

        frequencyGroup(
            "Division",
            active.map {
                it.details["Division"]
            }
        ),

        frequencyGroup(
            "Position",
            active.map {
                it.position
            }
        ),

        frequencyGroup(
            "Nationality",
            active.map {
                it.nationality
            }
        ),

        frequencyGroup(
            "Size",
            active.map {
                it.size
            }
        ),

        frequencyGroup(
            "Supplier",
            active.map {
                it.supplier
            }
        ),

        frequencyGroup(
            "Brand",
            active.map {
                it.brand
            }
        ),

        frequencyGroup(
            "Make",
            active.map {
                it.make
            }
        ),

        frequencyGroup(
            "Model",
            active.map {
                it.model
            }
        ),

        frequencyGroup(
            "First Season",
            active.map {
                it.firstSeason
            }
        ),

        frequencyGroup(
            "Last Season",
            active.map {
                it.lastSeason
            }
        ),

        frequencyGroup(
            "Colour",
            active.flatMap {
                it.colours
            }
        ),


        // DOB
        frequencyGroup(
            "DOB Month",
            active.map {

                dateMonthLabel(
                    it.details["DOB"]
                )
            }
        ),

        frequencyGroup(
            "DOB Year",
            active.map {

                dateYearLabel(
                    it.details["DOB"]
                )
            }
        ),


        // Order
        frequencyGroup(
            "Order Month",
            active.map {

                dateMonthLabel(
                    it.orderDate
                )
            }
        ),

        frequencyGroup(
            "Order Year",
            active.map {

                dateYearLabel(
                    it.orderDate
                )
            }
        ),


        // Receive
        frequencyGroup(
            "Receive Month",
            active.map {

                dateMonthLabel(
                    it.receiveDate
                )
            }
        ),

        frequencyGroup(
            "Receive Year",
            active.map {

                dateYearLabel(
                    it.receiveDate
                )
            }
        ),


        // Open
        frequencyGroup(
            "Open Month",
            active.map {

                dateMonthLabel(
                    it.openDate
                )
            }
        ),

        frequencyGroup(
            "Open Year",
            active.map {

                dateYearLabel(
                    it.openDate
                )
            }
        ),


        // Open
        frequencyGroup(
            "Manufacture Month",
            active.map {

                dateMonthLabel(
                    it.manufactureDate
                )
            }
        ),

        frequencyGroup(
            "Manufacture Year",
            active.map {

                dateYearLabel(
                    it.manufactureDate
                )
            }
        ),


        // Prices
        priceFrequencyGroup(
            "Sticker Price CAD",
            active.map {
                it.stickerPriceCAD()
            }
        ),

        priceFrequencyGroup(
            "Sticker Price CDN",
            active.map {
                it.stickerPriceCDN
            }
        ),

        priceFrequencyGroup(
            "Sticker Price US",
            active.map {
                it.stickerPriceUS
            }
        ),

        priceFrequencyGroup(
            "Shipping",
            active.map {
                it.shipping
            }
        ),

        priceFrequencyGroup(
            "Duty",
            active.map {
                it.duty
            }
        ),

        priceFrequencyGroup(
            "Discount",
            active.map {
                it.discount
            }
        ),

        priceFrequencyGroup(
            "Price M",
            active.map {
                it.priceM
            }
        ),

        priceFrequencyGroup(
            "Price C",
            active.map {
                it.priceC
            }
        ),

        priceFrequencyGroup(
            "Price F",
            active.map {
                it.priceF
            }
        )
    )
        .filter {

            it.mostCommon
                .isNotEmpty()
        }
}