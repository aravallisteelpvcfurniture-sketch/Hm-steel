package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

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

data class Visitor(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val company: String = "",
    val phone: String = "",
    val email: String = "",
    val source: VisitorSource = VisitorSource.EXPO_BOOTH,
    val interestLevel: InterestLevel = InterestLevel.WARM,
    val status: LeadStatus = LeadStatus.NEW,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val shortDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}
