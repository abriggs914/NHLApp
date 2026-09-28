package com.avery.nhl.model.nhl

import com.avery.nhl.model.jersey.Jersey
import com.avery.nhl.model.jersey.stickerPriceCAD
import com.avery.nhl.util.daysBetween
import com.avery.nhl.util.parseCollectionDate
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.collections.filter

data class CollectionStats(
    val jerseyCount: Int,

    val totalCost: Double,
    val pricedCount: Int,
    val averagePrice: Double?,

    val dollarsPerDay: Double?,
    val collectionAgeDays: Long?,

    val averageOrderToReceive: Double?,
    val averageReceiveToOpen: Double?,
    val averageOrderToOpen: Double?,

    val waitingToReceive: Int,
    val waitingToOpen: Int,

    val mostFrequentTeam: FrequencyStat?,
    val mostFrequentPosition: FrequencyStat?,
    val mostFrequentNationality: FrequencyStat?
)

fun calculateCollectionStats(
    jerseys: List<Jersey>,
    today: LocalDate = LocalDate.now()
): CollectionStats {

    val activeJerseys =
        jerseys.filter {
            !it.cancelled.equals(
                "true",
                ignoreCase = true
            )
        }

    // -----------------------------------------
    // Financial
    // -----------------------------------------

    val prices =
        activeJerseys
            .mapNotNull {
                it.priceF
            }

    val totalCost =
        prices.sum()

    val averagePrice =
        prices
            .takeIf { it.isNotEmpty() }
            ?.average()

    val firstOrder =
        activeJerseys
            .mapNotNull {
                parseCollectionDate(
                    it.orderDate
                )
            }
            .minOrNull()

    val collectionAgeDays =
        firstOrder?.let {
            ChronoUnit.DAYS.between(
                it,
                today
            ) + 1
        }

    val dollarsPerDay =
        collectionAgeDays
            ?.takeIf { it > 0 }
            ?.let {
                totalCost / it.toDouble()
            }


    // -----------------------------------------
    // Timing
    // -----------------------------------------

    val orderToReceive =
        activeJerseys
            .mapNotNull {
                daysBetween(
                    it.orderDate,
                    it.receiveDate
                )
            }
            .filter {
                it >= 0
            }

    val receiveToOpen =
        activeJerseys
            .mapNotNull {
                daysBetween(
                    it.receiveDate,
                    it.openDate
                )
            }
            .filter {
                it >= 0
            }

    val orderToOpen =
        activeJerseys
            .mapNotNull {
                daysBetween(
                    it.orderDate,
                    it.openDate
                )
            }
            .filter {
                it >= 0
            }


    // -----------------------------------------
    // Pending
    // -----------------------------------------

    val waitingToReceive =
        activeJerseys.count {
            !it.orderDate.isNullOrBlank() &&
                    it.receiveDate.isNullOrBlank()
        }

    val waitingToOpen =
        activeJerseys.count {
            !it.receiveDate.isNullOrBlank() &&
                    it.openDate.isNullOrBlank()
        }


    // -----------------------------------------
    // Frequencies
    // -----------------------------------------

    fun mostCommon(
        values: List<String?>
    ): FrequencyStat? {

        return values
            .mapNotNull {
                it
                    ?.trim()
                    ?.takeIf {
                            value ->
                        value.isNotBlank()
                    }
            }
            .groupingBy {
                it
            }
            .eachCount()
            .maxByOrNull {
                it.value
            }
            ?.let {
                FrequencyStat(
                    value = it.key,
                    count = it.value
                )
            }
    }

    return CollectionStats(
        jerseyCount =
            activeJerseys.size,

        totalCost =
            totalCost,

        pricedCount =
            prices.size,

        averagePrice =
            averagePrice,

        dollarsPerDay =
            dollarsPerDay,

        collectionAgeDays =
            collectionAgeDays,

        averageOrderToReceive =
            averageOrNull(
                orderToReceive
            ),

        averageReceiveToOpen =
            averageOrNull(
                receiveToOpen
            ),

        averageOrderToOpen =
            averageOrNull(
                orderToOpen
            ),

        waitingToReceive =
            waitingToReceive,

        waitingToOpen =
            waitingToOpen,

        mostFrequentTeam =
            mostCommon(
                activeJerseys.map {
                    it.team
                }
            ),

        mostFrequentPosition =
            mostCommon(
                activeJerseys.map {
                    it.position
                }
            ),

        mostFrequentNationality =
            mostCommon(
                activeJerseys.map {
                    it.nationality
                }
            )
    )
}

fun averageOrNull(
    values: List<Long>
): Double? {

    if (values.isEmpty()) {
        return null
    }

    return values.average()
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

        // -------------------------------------
        // Collection identity
        // -------------------------------------

        frequencyGroup(
            "Team",
            active.map { it.team }
        ),

        frequencyGroup(
            "League",
            active.map { it.league }
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
            active.map { it.position }
        ),

        frequencyGroup(
            "Nationality",
            active.map { it.nationality }
        ),

        frequencyGroup(
            "Size",
            active.map { it.size }
        ),

        frequencyGroup(
            "Supplier",
            active.map { it.supplier }
        ),

        frequencyGroup(
            "Brand",
            active.map { it.brand }
        ),

        frequencyGroup(
            "Make",
            active.map { it.make }
        ),

        frequencyGroup(
            "Model",
            active.map { it.model }
        ),

        frequencyGroup(
            "First Season",
            active.map { it.firstSeason }
        ),

        frequencyGroup(
            "Last Season",
            active.map { it.lastSeason }
        ),

        // A jersey contributes once for EACH colour
        frequencyGroup(
            "Colour",
            active.flatMap {
                it.colours
            }
        ),

        // -------------------------------------
        // DOB
        // -------------------------------------

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

        // -------------------------------------
        // Order
        // -------------------------------------

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

        // -------------------------------------
        // Receive
        // -------------------------------------

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

        // -------------------------------------
        // Open
        // -------------------------------------

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

        // -------------------------------------
        // Price distributions
        // -------------------------------------

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
            it.mostCommon.isNotEmpty()
        }
}

fun priceFrequencyGroup(
    title: String,
    values: List<Double?>
): FrequencyGroup {

    return frequencyGroup(
        title = title,
        values = values.map {
            priceBucket(it)
        }
    )
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
        ).toInt() * increment

    val upper =
        lower + increment

    return "$$lower–$$upper"
}

fun frequencyGroup(
    title: String,
    values: List<String?>
): FrequencyGroup {

    val items =
        frequencyItems(values)

    val most =
        items
            .sortedWith(
                compareByDescending<FrequencyItem> { it.count }
                    .thenBy { it.label.lowercase() }
            )
            .take(5)

    val least =
        items
            .sortedWith(
                compareBy<FrequencyItem> { it.count }
                    .thenBy { it.label.lowercase() }
            )
            .take(5)

    return FrequencyGroup(
        title = title,
        mostCommon = most,
        leastCommon = least
    )
}

fun frequencyItems(
    values: List<String?>
): List<FrequencyItem> {

    return values
        .mapNotNull { value ->
            value
                ?.trim()
                ?.takeIf { it.isNotBlank() && it != "-" }
        }
        .groupingBy { it }
        .eachCount()
        .map { (label, count) ->
            FrequencyItem(
                label = label,
                count = count
            )
        }
}

fun dateMonthLabel(
    value: String?
): String? {

    val date =
        parseCollectionDate(value)
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

    return parseCollectionDate(value)
        ?.year
        ?.toString()
}