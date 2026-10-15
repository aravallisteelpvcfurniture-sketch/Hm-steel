package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MeasurementUnit(val code: String, val label: String, val areaLabel: String) {
    FEET("ft", "Feet (ft)", "sq ft"),
    METERS("m", "Meters (m)", "sq m"),
    INCHES("in", "Inches (in)", "sq in")
}

@IgnoreExtraProperties
data class Measurement(
    @DocumentId val id: String = "",
    val locationName: String = "",
    val clientOrVisitorId: String = "",
    val clientName: String = "",
    val length: Double = 0.0,
    val width: Double = 0.0,
    val height: Double = 0.0,
    val unitCode: String = MeasurementUnit.FEET.name,
    val notes: String = "",
    val photoUri: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    constructor(
        id: String = "",
        locationName: String = "",
        clientOrVisitorId: String = "",
        clientName: String = "",
        length: Double = 0.0,
        width: Double = 0.0,
        height: Double = 0.0,
        unit: MeasurementUnit = MeasurementUnit.FEET,
        notes: String = "",
        photoUri: String? = null,
        createdAt: Timestamp? = null,
        updatedAt: Timestamp? = null
    ) : this(
        id = id,
        locationName = locationName,
        clientOrVisitorId = clientOrVisitorId,
        clientName = clientName,
        length = length,
        width = width,
        height = height,
        unitCode = unit.name,
        notes = notes,
        photoUri = photoUri,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    val unit: MeasurementUnit
        get() = runCatching { MeasurementUnit.valueOf(unitCode) }.getOrDefault(MeasurementUnit.FEET)

    val floorArea: Double
        get() = length * width

    val perimeter: Double
        get() = 2 * (length + width)

    val wallSurfaceArea: Double
        get() = if (height > 0) 2 * (length + width) * height else 0.0

    val volume: Double
        get() = if (height > 0) length * width * height else 0.0

    val flooringBoxesEstimate: Int
        get() {
            val coveragePerBox = if (unit == MeasurementUnit.METERS) 2.0 else 20.0
            val neededWithWastage = floorArea * 1.10
            return kotlin.math.ceil(neededWithWastage / coveragePerBox).toInt().coerceAtLeast(1)
        }

    val paintGallonsEstimate: Double
        get() {
            val targetArea = if (wallSurfaceArea > 0) wallSurfaceArea else floorArea
            val coverage = if (unit == MeasurementUnit.METERS) 32.5 else 350.0
            val gallons = (targetArea * 2) / coverage
            return (gallons * 10).toInt() / 10.0
        }

    val formattedDate: String
        get() {
            val date = createdAt?.toDate() ?: Date()
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return sdf.format(date)
        }
}
