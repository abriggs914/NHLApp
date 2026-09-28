package com.avery.nhl.model.jersey

enum class JerseySort {
    ID,
    LEAGUE,
    TEAM,
    PLAYER,
    NUMBER,
    BRAND,
    MAKE,
    MODEL,
    SUPPLIER,
    SIZE,

    COLOUR1,
    COLOUR2,
    COLOUR3,
    COLOURS,

    DOB,
    ORDER_DATE,
    RECEIVE_DATE,
    OPEN_DATE,

    MANUFACTURE_DATE,

    NATIONALITY,
    POSITION,
    APATCH,
    CPATCH,
    FIRSTSEASON,
    LASTSEASON,

    IMAGE_COUNT
}

fun JerseySort.displayName(): String {
    return when (this) {
        JerseySort.ID -> "ID"
        JerseySort.LEAGUE -> "League"
        JerseySort.TEAM -> "Team"
        JerseySort.PLAYER -> "Player"
        JerseySort.NUMBER -> "Jersey Number"

        JerseySort.BRAND -> "Brand"
        JerseySort.MAKE -> "Make"
        JerseySort.MODEL -> "Style / Model"
        JerseySort.SUPPLIER -> "Supplier"
        JerseySort.SIZE -> "Size"

        JerseySort.COLOUR1 -> "Primary Colour"
        JerseySort.COLOUR2 -> "Secondary Colour"
        JerseySort.COLOUR3 -> "Tertiary Colour"
        JerseySort.COLOURS -> "Colour Combination"

        JerseySort.DOB -> "DOB"
        JerseySort.ORDER_DATE -> "Order Date"
        JerseySort.RECEIVE_DATE -> "Receive Date"
        JerseySort.OPEN_DATE -> "Open Date"
        JerseySort.MANUFACTURE_DATE -> "Manufacture Date"

        JerseySort.NATIONALITY -> "Nationality"
        JerseySort.POSITION -> "Position"
        JerseySort.FIRSTSEASON -> "First Season"
        JerseySort.LASTSEASON -> "Last Season"
        JerseySort.CPATCH -> "Captain Patch"
        JerseySort.APATCH -> "Assistant Patch"

        JerseySort.IMAGE_COUNT -> "Image Count"
    }
}