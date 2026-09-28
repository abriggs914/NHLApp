package com.avery.nhl.model.jersey

import androidx.compose.ui.graphics.Color
import com.avery.nhl.model.nhl.ManufactureMonth
import java.time.LocalDate
import java.util.Calendar
import java.util.Date


data class Jersey(
    val id: Int,
    val cancelled: String?,
    val league: String?,
    val team: String,
    val number: Int?,
    val playerFirst: String?,
    val playerLast: String?,

    val brand: String?,
    val model: String?,
    val make: String?,
    val supplier: String?,

    val orderDate: String?,
    val receiveDate: String?,
    val openDate: String?,

    val colour1: String?,
    val colour2: String?,
    val colour3: String?,
    val size: String?,

    val nationality: String?,
    val position: String?,
    val cPatch: String?,
    val aPatch: String?,

    val firstSeason: String?,
    val lastSeason: String?,

    val usSale: String?,
    val stickerPriceCDN: Double?,
    val stickerPriceUS: Double?,
    val shipping: Double?,
    val duty: Double?,
    val discount: Double?,
    val priceM: Double?,
    val priceC: Double?,

    val images: List<String>,

    // Keeps every field from collections.json
    val details: Map<String, String?>
) {
    val playerName: String?
        get() {
            val parts = listOfNotNull(
                playerFirst?.takeIf { it.isNotBlank() },
                playerLast?.takeIf { it.isNotBlank() }
            )

            return parts
                .joinToString(" ")
                .takeIf { it.isNotBlank() }
        }

    val hasPlayer: Boolean
        get() = !playerName.isNullOrBlank()

    val colours: List<String>
        get() = listOfNotNull(
            colour1,
            colour2,
            colour3
        )
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

    val nhlId: Long?
        get() =
            details["NHLID"]
                ?.toDoubleOrNull()
                ?.toLong()

    val priceF: Double
        get() {

            val exchangeRate =
                details["ExchangeRate"]
                    ?.toDoubleOrNull()
                    ?: 0.0

            val tax =
                details["Tax"]
                    ?.toDoubleOrNull()
                    ?: 0.0

            val shippingValue =
                shipping ?: 0.0

            val dutyValue =
                duty ?: 0.0

            val discountValue =
                discount ?: 0.0

            return when {

                // US-priced jersey
                stickerPriceUS != null &&
                        stickerPriceUS != 0.0 -> {

                    (
                            exchangeRate *
                                    (
                                            stickerPriceUS +
                                                    shippingValue +
                                                    tax
                                            )
                            ) +
                            dutyValue -
                            discountValue
                }

                // Canadian-priced jersey
                stickerPriceCDN != null &&
                        stickerPriceCDN != 0.0 -> {

                    stickerPriceCDN +
                            dutyValue +
                            shippingValue +
                            tax -
                            discountValue
                }

                // No usable price
                else -> -1.0
            }
        }

    val manufactureMonth: ManufactureMonth?
        get() = parseManufactureDate(
            details[
                "ManufactureDate"
            ]
        )

    val manufactureDate: String?
        get() {
            val y =
                manufactureMonth?.year
            val m =
                manufactureMonth?.month
            val d =
                1
            return when {
                (y == null) || (m == null) -> {
                    null
                }
                else -> {
                    LocalDate.of(y, m, d).toString()
                }

            }

        }
}

fun Jersey.toDisplayString(
    includeTeam: Boolean = true,
    includeBrand: Boolean = true,
    includeMake: Boolean = true,
    includeModel: Boolean = true,
    includeNumber: Boolean = hasPlayer,
    includeFirstName: Boolean = hasPlayer,
    includeLastName: Boolean = hasPlayer,
    includeSize: Boolean = true
): String {

    val parts =
        mutableListOf<String>()

    fun add(
        value: String?
    ) {
        value
            ?.trim()
            ?.takeIf {
                it.isNotBlank() &&
                        it != "-"
            }
            ?.let {
                parts.add(it)
            }
    }

    if (includeTeam) {
        add(team)
    }

    if (includeBrand) {
        add(brand)
    }

    if (includeMake) {
        add(make)
    }

    if (includeModel) {
        add(model)
    }

    if (
        includeNumber &&
        number != null
    ) {
        parts.add(
            "#$number"
        )
    }

    if (includeFirstName) {
        add(playerFirst)
    }

    if (includeLastName) {
        add(playerLast)
    }

    if (includeSize) {
        add(size)
    }

    return parts.joinToString(" ")
}

fun jerseyColour(
    value: String
): Color {

    val name = value
        .trim()
        .lowercase()
        .replace("-", " ")
        .replace("_", " ")

    return when (name) {

        "black" ->
            Color.Black

        "white" ->
            Color.White

        "red" ->
            Color(0xFFD32F2F)

        "dark red",
        "maroon" ->
            Color(0xFF800000)

        "blue" ->
            Color(0xFF1976D2)

        "dark blue",
        "navy",
        "navy blue" ->
            Color(0xFF0D2B56)

        "light blue",
        "powder blue" ->
            Color(0xFF87CEEB)

        "green" ->
            Color(0xFF388E3C)

        "dark green" ->
            Color(0xFF1B5E20)

        "yellow" ->
            Color(0xFFFFD600)

        "gold" ->
            Color(0xFFFFC107)

        "orange" ->
            Color(0xFFF57C00)

        "purple" ->
            Color(0xFF7B1FA2)

        "pink" ->
            Color(0xFFE91E63)

        "grey",
        "gray" ->
            Color(0xFF808080)

        "silver" ->
            Color(0xFFC0C0C0)

        "cream" ->
            Color(0xFFFFFDD0)

        "beige" ->
            Color(0xFFF5F5DC)

        "brown" ->
            Color(0xFF795548)

        else -> {

            // Also support hex values in your data,
            // e.g. #C8102E
            try {

                if (
                    value.startsWith("#") &&
                    value.length == 7
                ) {
                    Color(
                        android.graphics.Color.parseColor(value)
                    )
                } else {
                    Color.LightGray
                }

            } catch (_: Exception) {
                Color.LightGray
            }
        }
    }
}

fun Jersey.stickerPriceCAD(): Double? {
    return details["StickerPriceCAD"]
        ?.toDoubleOrNull()
}
