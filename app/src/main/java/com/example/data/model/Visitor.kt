package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class VisitorSource(val label: String) {
    EXPO_BOOTH("Trade Show / Expo"),
    STOREFRONT("Storefront / Walk-in"),
    DIGITAL_CAMPAIGN("Social & Digital Ads"),
    REFERRAL("Client Referral"),
    EVENT("Marketing Event"),
    OTHER("Other")
}

enum class InterestLevel(val label: String) {
    HOT("High Intent (Hot)"),
    WARM("Interested (Warm)"),
    COLD("Inquiry (Cold)")
}

enum class LeadStatus(val label: String) {
    NEW("New Visitor"),
    CONTACTED("Contacted"),
    SITE_VISIT_BOOKED("Measurement Booked"),
    ESTIMATE_SENT("Estimate Sent"),
    CONVERTED("Deal Closed"),
    DROPPED("Archived")
}

@IgnoreExtraProperties
data class Visitor(
    @DocumentId val id: String = "",
    val name: String = "",
    val company: String = "",
    val phone: String = "",
    val email: String = "",
    val sourceName: String = VisitorSource.EXPO_BOOTH.name,
    val interestLevelName: String = InterestLevel.WARM.name,
    val statusName: String = LeadStatus.NEW.name,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    constructor(
        id: String = "",
        name: String = "",
        company: String = "",
        phone: String = "",
        email: String = "",
        source: VisitorSource = VisitorSource.EXPO_BOOTH,
        interestLevel: InterestLevel = InterestLevel.WARM,
        status: LeadStatus = LeadStatus.NEW,
        notes: String = "",
        createdAt: Timestamp? = null,
        updatedAt: Timestamp? = null
    ) : this(
        id = id,
        name = name,
        company = company,
        phone = phone,
        email = email,
        sourceName = source.name,
        interestLevelName = interestLevel.name,
        statusName = status.name,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    val source: VisitorSource
        get() = runCatching { VisitorSource.valueOf(sourceName) }.getOrDefault(VisitorSource.EXPO_BOOTH)

    val interestLevel: InterestLevel
        get() = runCatching { InterestLevel.valueOf(interestLevelName) }.getOrDefault(InterestLevel.WARM)

    val status: LeadStatus
        get() = runCatching { LeadStatus.valueOf(statusName) }.getOrDefault(LeadStatus.NEW)

    val formattedDate: String
        get() {
            val date = createdAt?.toDate() ?: Date()
            val sdf = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
            return sdf.format(date)
        }

    val shortDate: String
        get() {
            val date = createdAt?.toDate() ?: Date()
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            return sdf.format(date)
        }
}
