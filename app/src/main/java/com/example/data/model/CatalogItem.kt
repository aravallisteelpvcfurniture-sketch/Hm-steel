package com.example.data.model

import java.util.UUID

enum class CatalogCategory(val label: String) {
    SIGNAGE("Signage & Banners"),
    BOOTH_FABRICATION("Booth & Displays"),
    PRINT_MEDIA("Print & Collateral"),
    DIGITAL_SERVICES("Digital & Video"),
    INSTALLATION_LABOR("Installation & Labor"),
    MATERIALS("Raw Materials & Fixtures")
}

data class CatalogItem(
    val id: String = UUID.randomUUID().toString(),
    val sku: String,
    val title: String,
    val category: CatalogCategory,
    val unitPrice: Double,
    val unit: String, // "sq ft", "unit", "hr", "package"
    val description: String = "",
    val inStock: Boolean = true,
    val tags: List<String> = emptyList()
)
