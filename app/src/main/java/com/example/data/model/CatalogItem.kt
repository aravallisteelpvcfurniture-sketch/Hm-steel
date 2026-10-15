package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class CatalogCategory(val label: String) {
    SIGNAGE("Signage & Banners"),
    BOOTH_FABRICATION("Booth & Displays"),
    PRINT_MEDIA("Print & Collateral"),
    DIGITAL_SERVICES("Digital & Video"),
    INSTALLATION_LABOR("Installation & Labor"),
    MATERIALS("Raw Materials & Fixtures")
}

@IgnoreExtraProperties
data class CatalogItem(
    @DocumentId val id: String = "",
    val sku: String = "",
    val title: String = "",
    val categoryName: String = CatalogCategory.SIGNAGE.name,
    val unitPrice: Double = 0.0,
    val unit: String = "unit",
    val description: String = "",
    val inStock: Boolean = true,
    val tags: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    constructor(
        id: String = "",
        sku: String = "",
        title: String = "",
        category: CatalogCategory = CatalogCategory.SIGNAGE,
        unitPrice: Double = 0.0,
        unit: String = "unit",
        description: String = "",
        inStock: Boolean = true,
        tags: List<String> = emptyList(),
        createdAt: Timestamp? = null,
        updatedAt: Timestamp? = null
    ) : this(
        id = id,
        sku = sku,
        title = title,
        categoryName = category.name,
        unitPrice = unitPrice,
        unit = unit,
        description = description,
        inStock = inStock,
        tags = tags,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    val category: CatalogCategory
        get() = runCatching { CatalogCategory.valueOf(categoryName) }.getOrDefault(CatalogCategory.SIGNAGE)
}
