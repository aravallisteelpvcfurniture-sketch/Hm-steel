package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class EstimateStatus(val label: String) {
    DRAFT("Draft"),
    SENT("Sent to Client"),
    ACCEPTED("Accepted / Won"),
    REJECTED("Declined"),
    COMPLETED("Fulfilled & Paid")
}

@IgnoreExtraProperties
data class EstimateItem(
    val id: String = "",
    val title: String = "",
    val category: String = "Materials",
    val quantity: Double = 1.0,
    val unit: String = "units",
    val unitPrice: Double = 0.0
) {
    val total: Double
        get() = quantity * unitPrice
}

@IgnoreExtraProperties
data class Estimate(
    @DocumentId val id: String = "",
    val quoteNumber: String = "",
    val clientName: String = "",
    val clientContact: String = "",
    val projectTitle: String = "",
    val linkedMeasurementId: String? = null,
    val items: List<EstimateItem> = emptyList(),
    val taxRatePercent: Double = 8.5,
    val markupPercent: Double = 15.0,
    val discountPercent: Double = 0.0,
    val statusName: String = EstimateStatus.DRAFT.name,
    val notes: String = "Thank you for considering our proposal. Valid for 30 days.",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    constructor(
        id: String = "",
        quoteNumber: String = "",
        clientName: String = "",
        clientContact: String = "",
        projectTitle: String = "",
        linkedMeasurementId: String? = null,
        items: List<EstimateItem> = emptyList(),
        taxRatePercent: Double = 8.5,
        markupPercent: Double = 15.0,
        discountPercent: Double = 0.0,
        status: EstimateStatus = EstimateStatus.DRAFT,
        notes: String = "Thank you for considering our proposal. Valid for 30 days.",
        createdAt: Timestamp? = null,
        updatedAt: Timestamp? = null
    ) : this(
        id = id,
        quoteNumber = quoteNumber,
        clientName = clientName,
        clientContact = clientContact,
        projectTitle = projectTitle,
        linkedMeasurementId = linkedMeasurementId,
        items = items,
        taxRatePercent = taxRatePercent,
        markupPercent = markupPercent,
        discountPercent = discountPercent,
        statusName = status.name,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    val status: EstimateStatus
        get() = runCatching { EstimateStatus.valueOf(statusName) }.getOrDefault(EstimateStatus.DRAFT)

    val subtotal: Double
        get() = items.sumOf { it.total }

    val markupAmount: Double
        get() = subtotal * (markupPercent / 100.0)

    val discountedSubtotal: Double
        get() = (subtotal + markupAmount) * (1.0 - (discountPercent / 100.0))

    val taxAmount: Double
        get() = discountedSubtotal * (taxRatePercent / 100.0)

    val grandTotal: Double
        get() = discountedSubtotal + taxAmount

    val formattedDate: String
        get() {
            val date = createdAt?.toDate() ?: Date()
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return sdf.format(date)
        }

    fun toShareableText(currencySymbol: String = "$"): String {
        val sb = StringBuilder()
        sb.append("📋 ESTIMATE / QUOTATION: $quoteNumber\n")
        sb.append("Project: $projectTitle\n")
        sb.append("Client: $clientName\n")
        sb.append("Date: $formattedDate | Status: ${status.label}\n\n")
        sb.append("--- LINE ITEMS ---\n")
        items.forEachIndexed { i, item ->
            sb.append("${i + 1}. ${item.title} (${item.category})\n")
            sb.append("   ${item.quantity} ${item.unit} @ $currencySymbol${String.format(Locale.US, "%.2f", item.unitPrice)} = $currencySymbol${String.format(Locale.US, "%.2f", item.total)}\n")
        }
        sb.append("\n------------------\n")
        sb.append("Subtotal: $currencySymbol${String.format(Locale.US, "%.2f", subtotal)}\n")
        if (markupPercent > 0) {
            sb.append("Markup ($markupPercent%): +$currencySymbol${String.format(Locale.US, "%.2f", markupAmount)}\n")
        }
        if (discountPercent > 0) {
            sb.append("Discount ($discountPercent%): -$currencySymbol${String.format(Locale.US, "%.2f", (subtotal + markupAmount) * (discountPercent / 100.0))}\n")
        }
        sb.append("Tax ($taxRatePercent%): +$currencySymbol${String.format(Locale.US, "%.2f", taxAmount)}\n")
        sb.append("GRAND TOTAL: $currencySymbol${String.format(Locale.US, "%.2f", grandTotal)}\n\n")
        sb.append("Notes: $notes\n")
        return sb.toString()
    }
}
