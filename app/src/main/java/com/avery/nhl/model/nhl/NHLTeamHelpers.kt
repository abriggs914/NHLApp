package com.avery.nhl.model.nhl


fun teamNameToAbbrev(
    teamName: String
): String? {

    return when (
        teamName
            .trim()
            .lowercase()
    ) {

        "anaheim ducks" ->
            "ANA"

        "arizona coyotes" ->
            "ARI"

        "boston bruins" ->
            "BOS"

        "buffalo sabres" ->
            "BUF"

        "calgary flames" ->
            "CGY"

        "carolina hurricanes" ->
            "CAR"

        "chicago blackhawks" ->
            "CHI"

        "colorado avalanche" ->
            "COL"

        "columbus blue jackets" ->
            "CBJ"

        "dallas stars" ->
            "DAL"

        "detroit red wings" ->
            "DET"

        "edmonton oilers" ->
            "EDM"

        "florida panthers" ->
            "FLA"

        "los angeles kings" ->
            "LAK"

        "minnesota wild" ->
            "MIN"

        "montreal canadiens" ->
            "MTL"

        "nashville predators" ->
            "NSH"

        "new jersey devils" ->
            "NJD"

        "new york islanders" ->
            "NYI"

        "new york rangers" ->
            "NYR"

        "ottawa senators" ->
            "OTT"

        "philadelphia flyers" ->
            "PHI"

        "pittsburgh penguins" ->
            "PIT"

        "san jose sharks" ->
            "SJS"

        "seattle kraken" ->
            "SEA"

        "st. louis blues" ->
            "STL"

        "tampa bay lightning" ->
            "TBL"

        "toronto maple leafs" ->
            "TOR"

        "utah hockey club",
        "utah mammoth" ->
            "UTA"

        "vancouver canucks" ->
            "VAN"

        "vegas golden knights" ->
            "VGK"

        "washington capitals" ->
            "WSH"

        "winnipeg jets" ->
            "WPG"

        else ->
            null
    }
}