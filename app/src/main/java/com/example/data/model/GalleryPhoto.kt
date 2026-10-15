package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class PhotoCategory(val label: String) {
    EXPO_BOOTH("Trade Show Booth"),
    SITE_SURVEY("Site Measurement"),
    BEFORE_AFTER("Before & After"),
    INSTALLATION("Finished Install"),
    MARKETING("Marketing Collateral"),
    CATALOG_SAMPLE("Catalog Showcase")
}

data class GalleryPhoto(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: PhotoCategory = PhotoCategory.EXPO_BOOTH,
    val photoUri: String? = null, // URI from PhotoPicker or null if vector template
    val vectorIconName: String = "trade_show", // for built-in visual card templates
    val clientName: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}
