package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class MeasurementUnit(val code: String, val label: String, val areaLabel: String) {
    FEET("ft", "Feet (ft)", "sq ft"),
    METERS("m", "Meters (m)", "sq m"),
    INCHES("in", "Inches (in)", "sq in")
}

data class Measurement(
    val id: String = UUID.randomUUID().toString(),
    val locationName: String, // e.g. "Booth 4B Display Wall", "Client Main Lobby", "Conference Room"
    val clientOrVisitorId: String = "",
    val clientName: String = "",
    val length: Double,
    val width: Double,
    val height: Double = 0.0,
    val unit: MeasurementUnit = MeasurementUnit.FEET,
    val notes: String = "",
    val photoUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    // Basic Geometry
    val floorArea: Double
        get() = length * width

    val perimeter: Double
        get() = 2 * (length + width)

    val wallSurfaceArea: Double
        get() = if (height > 0) 2 * (length + width) * height else 0.0

    val volume: Double
        get() = if (height > 0) length * width * height else 0.0

    // Material Estimations
    val flooringBoxesEstimate: Int
        get() {
            // Assume 20 sq ft (or ~2 sq m) per box + 10% wastage
            val coveragePerBox = if (unit == MeasurementUnit.METERS) 2.0 else 20.0
            val neededWithWastage = floorArea * 1.10
            return kotlin.math.ceil(neededWithWastage / coveragePerBox).toInt().coerceAtLeast(1)
        }

    val paintGallonsEstimate: Double
        get() {
            // Standard: ~350 sq ft per gallon (or ~32 sq m per gallon) for 2 coats
            val targetArea = if (wallSurfaceArea > 0) wallSurfaceArea else floorArea
            val coverage = if (unit == MeasurementUnit.METERS) 32.5 else 350.0
            val gallons = (targetArea * 2) / coverage
            return (gallons * 10).toInt() / 10.0
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}
