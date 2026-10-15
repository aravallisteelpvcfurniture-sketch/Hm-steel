package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class AppSettings(
    val businessName: String = "Apex Marketing & Fabrication",
    val businessTagline: String = "Precision Measurements, Estimates & Displays",
    val phone: String = "+1 (555) 389-4400",
    val email: String = "contact@apexmarketing.com",
    val address: String = "100 Innovation Way, Suite 400",
    val currencySymbol: String = "$",
    val defaultUnitName: String = MeasurementUnit.FEET.name,
    val defaultTaxRate: Double = 8.5,
    val defaultMarkupRate: Double = 15.0,
    val dailyVisitorTarget: Int = 50,
    val isDarkMode: Boolean = false,
    val useDynamicColors: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    constructor(
        businessName: String = "Apex Marketing & Fabrication",
        businessTagline: String = "Precision Measurements, Estimates & Displays",
        phone: String = "+1 (555) 389-4400",
        email: String = "contact@apexmarketing.com",
        address: String = "100 Innovation Way, Suite 400",
        currencySymbol: String = "$",
        defaultUnit: MeasurementUnit = MeasurementUnit.FEET,
        defaultTaxRate: Double = 8.5,
        defaultMarkupRate: Double = 15.0,
        dailyVisitorTarget: Int = 50,
        isDarkMode: Boolean = false,
        useDynamicColors: Boolean = false,
        createdAt: Timestamp? = null,
        updatedAt: Timestamp? = null
    ) : this(
        businessName = businessName,
        businessTagline = businessTagline,
        phone = phone,
        email = email,
        address = address,
        currencySymbol = currencySymbol,
        defaultUnitName = defaultUnit.name,
        defaultTaxRate = defaultTaxRate,
        defaultMarkupRate = defaultMarkupRate,
        dailyVisitorTarget = dailyVisitorTarget,
        isDarkMode = isDarkMode,
        useDynamicColors = useDynamicColors,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    val defaultUnit: MeasurementUnit
        get() = runCatching { MeasurementUnit.valueOf(defaultUnitName) }.getOrDefault(MeasurementUnit.FEET)
}
