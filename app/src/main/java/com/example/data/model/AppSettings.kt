package com.example.data.model

data class AppSettings(
    val businessName: String = "Apex Marketing & Fabrication",
    val businessTagline: String = "Precision Measurements, Estimates & Displays",
    val phone: String = "+1 (555) 389-4400",
    val email: String = "contact@apexmarketing.com",
    val address: String = "100 Innovation Way, Suite 400",
    val currencySymbol: String = "$",
    val defaultUnit: MeasurementUnit = MeasurementUnit.FEET,
    val defaultTaxRate: Double = 8.5,
    val defaultMarkupRate: Double = 15.0,
    val dailyVisitorTarget: Int = 50,
    val isDarkMode: Boolean = false,
    val useDynamicColors: Boolean = false
)
