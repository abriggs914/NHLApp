package com.avery.nhl.model.nhl

enum class AnniversaryType(
    val displayName: String
) {
    ORDER("Ordered"),
    RECEIVE("Received"),
    OPEN("Opened"),
    DOB("Birthday"),
    MANUFACTURE("Manufactured")
}