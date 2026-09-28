package com.avery.nhl.data.collection

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.avery.nhl.model.jersey.Jersey
import org.json.JSONArray
import org.json.JSONObject

class JerseyRepository(
    private val context: Context
) {

    fun loadJerseys(): List<Jersey> {

        val jsonText = context.assets
            .open("collections.json")
            .bufferedReader()
            .use { it.readText() }

        val jsonArray = JSONArray(jsonText)

        val jerseys = mutableListOf<Jersey>()

        for (i in 0 until jsonArray.length()) {

            val obj = jsonArray.getJSONObject(i)

            val images = mutableListOf<String>()

            val imageArray = obj.optJSONArray("images")

            if (imageArray != null) {
                for (j in 0 until imageArray.length()) {
                    images.add(
                        imageArray.getString(j)
                    )
                }
            }

            val details =
                mutableMapOf<String, String?>()

            val keys = obj.keys()

            while (keys.hasNext()) {

                val key = keys.next()

                if (key != "images") {

                    details[key] =
                        if (obj.isNull(key)) {
                            null
                        } else {
                            obj.get(key).toString()
                        }
                }
            }

            jerseys.add(
                Jersey(
                    id = obj.getInt("ID"),

                    cancelled =
                        obj.optString("Cancelled"),

                    league =
                        obj.optNullableString("League"),

                    team =
                        obj.optNullableString("Team")
                            ?: "Unknown Team",

                    number =
                        if (obj.isNull("Number")) {
                            null
                        } else {
                            obj.optDouble("Number")
                                .toInt()
                        },

                    playerFirst =
                        obj.optNullableString(
                            "PlayerFirst"
                        ),

                    playerLast =
                        obj.optNullableString(
                            "PlayerLast"
                        ),

                    brand =
                        obj.optNullableString("Brand"),

                    make =
                        obj.optNullableString("Make"),

                    model =
                        obj.optNullableString("Model"),

                    supplier =
                        obj.optNullableString("Supplier"),

                    nationality =
                        obj.optNullableString(
                            "Nationality"
                        ),

                    position =
                        obj.optNullableString(
                            "Position"
                        ),

                    aPatch =
                        obj.optNullableString("APatch"),

                    cPatch =
                        obj.optNullableString("CPatch"),

                    stickerPriceCDN =
                        if (
                            obj.isNull(
                                "StickerPriceCDN"
                            )
                        ) {
                            null
                        } else {
                            obj.optDouble(
                                "StickerPriceCDN"
                            )
                        },

                    stickerPriceUS =
                        if (
                            obj.isNull(
                                "StickerPriceUS"
                            )
                        ) {
                            null
                        } else {
                            obj.optDouble(
                                "StickerPriceUS"
                            )
                        },

                    duty =
                        if (obj.isNull("Duty")) {
                            null
                        } else {
                            obj.optDouble("Duty")
                        },

                    shipping =
                        if (obj.isNull("Shipping")) {
                            null
                        } else {
                            obj.optDouble("Shipping")
                        },

                    discount =
                        if (obj.isNull("Discount")) {
                            null
                        } else {
                            obj.optDouble("Discount")
                        },

                    priceM =
                        if (obj.isNull("PriceM")) {
                            null
                        } else {
                            obj.optDouble("PriceM")
                        },

                    priceC =
                        if (obj.isNull("PriceC")) {
                            null
                        } else {
                            obj.optDouble("PriceC")
                        },

                    usSale =
                        obj.optNullableString("USSale"),

                    firstSeason =
                        obj.optNullableString(
                            "FirstSeason"
                        ),

                    lastSeason =
                        obj.optNullableString(
                            "LastSeason"
                        ),

                    orderDate =
                        obj.optNullableString(
                            "OrderDate"
                        ),

                    receiveDate =
                        obj.optNullableString(
                            "ReceiveDate"
                        ),

                    openDate =
                        obj.optNullableString(
                            "OpenDate"
                        ),

                    colour1 =
                        obj.optNullableString("Colour1"),

                    colour2 =
                        obj.optNullableString("Colour2"),

                    colour3 =
                        obj.optNullableString("Colour3"),

                    size =
                        obj.optNullableString("Size"),

                    images = images,
                    details = details
                )
            )
        }

        return jerseys
    }


    fun loadImageUris(): Map<String, Uri> {

        val imageMap =
            mutableMapOf<String, Uri>()

        val collection =
            MediaStore.Images.Media
                .EXTERNAL_CONTENT_URI

        val projection =
            arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME
            )

        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            null
        )?.use { cursor ->

            val idColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Images.Media._ID
                )

            val nameColumn =
                cursor.getColumnIndexOrThrow(
                    MediaStore.Images.Media.DISPLAY_NAME
                )

            while (cursor.moveToNext()) {

                val id =
                    cursor.getLong(idColumn)

                val fileName =
                    cursor.getString(nameColumn)

                val uri =
                    ContentUris.withAppendedId(
                        collection,
                        id
                    )

                imageMap[fileName] = uri
            }
        }

        return imageMap
    }
}


private fun JSONObject.optNullableString(
    name: String
): String? {

    if (!has(name) || isNull(name)) {
        return null
    }

    return optString(name)
        .takeIf { it.isNotBlank() }
}