package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class AppRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("marketscope_prefs", Context.MODE_PRIVATE)

    private val _visitors = MutableStateFlow<List<Visitor>>(emptyList())
    val visitors: StateFlow<List<Visitor>> = _visitors.asStateFlow()

    private val _measurements = MutableStateFlow<List<Measurement>>(emptyList())
    val measurements: StateFlow<List<Measurement>> = _measurements.asStateFlow()

    private val _estimates = MutableStateFlow<List<Estimate>>(emptyList())
    val estimates: StateFlow<List<Estimate>> = _estimates.asStateFlow()

    private val _catalog = MutableStateFlow<List<CatalogItem>>(emptyList())
    val catalog: StateFlow<List<CatalogItem>> = _catalog.asStateFlow()

    private val _gallery = MutableStateFlow<List<GalleryPhoto>>(emptyList())
    val gallery: StateFlow<List<GalleryPhoto>> = _gallery.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    // Quick footfall clicker counter for current day
    private val _quickVisitorCount = MutableStateFlow(0)
    val quickVisitorCount: StateFlow<Int> = _quickVisitorCount.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val hasData = prefs.getBoolean("has_initialized_v2", false)
        if (!hasData) {
            seedSampleData()
        } else {
            loadFromPrefs()
        }
    }

    private fun seedSampleData() {
        val sampleCatalog = listOf(
            CatalogItem(
                sku = "SIG-LED-01",
                title = "Backlit LED Fabric Signage Wall",
                category = CatalogCategory.SIGNAGE,
                unitPrice = 45.0,
                unit = "sq ft",
                description = "Seamless edge-lit aluminum frame with tension fabric graphic print. High brightness.",
                tags = listOf("LED", "Backlit", "Fabric")
            ),
            CatalogItem(
                sku = "BTH-MOD-10",
                title = "10x10 Modular Truss Booth System",
                category = CatalogCategory.BOOTH_FABRICATION,
                unitPrice = 3200.0,
                unit = "unit",
                description = "Lightweight steel/aluminum truss system with monitor mounts, podium & spot lighting.",
                tags = listOf("Truss", "Modular", "TradeShow")
            ),
            CatalogItem(
                sku = "PRN-BNR-XL",
                title = "Retractable Premium Roll-Up Banner",
                category = CatalogCategory.PRINT_MEDIA,
                unitPrice = 165.0,
                unit = "unit",
                description = "33x81 inches heavy duty aluminum base with vibrant anti-curl matte vinyl graphic.",
                tags = listOf("Banner", "Portable", "Expo")
            ),
            CatalogItem(
                sku = "FLR-VIN-WD",
                title = "Interlocking Wood Grain Flooring Tiles",
                category = CatalogCategory.MATERIALS,
                unitPrice = 6.50,
                unit = "sq ft",
                description = "High density anti-fatigue EVA foam tiles with commercial protective finish.",
                tags = listOf("Flooring", "Tiles", "Booth")
            ),
            CatalogItem(
                sku = "DIG-DIS-55",
                title = "55-inch 4K Interactive Touch Kiosk",
                category = CatalogCategory.DIGITAL_SERVICES,
                unitPrice = 1250.0,
                unit = "unit",
                description = "Self-standing interactive kiosk with capacitive touch and marketing video player.",
                tags = listOf("Kiosk", "Touch", "Digital")
            ),
            CatalogItem(
                sku = "LBR-INST-01",
                title = "On-Site Installation & Assembly Team",
                category = CatalogCategory.INSTALLATION_LABOR,
                unitPrice = 85.0,
                unit = "hr",
                description = "Certified installation crew including dismantle, packing, and electrical check.",
                tags = listOf("Labor", "Setup", "Service")
            )
        )
        _catalog.value = sampleCatalog

        val sampleVisitors = listOf(
            Visitor(
                name = "Eleanor Vance",
                company = "Vance Global Logistics",
                phone = "(555) 234-8901",
                email = "e.vance@vancelogistics.com",
                source = VisitorSource.EXPO_BOOTH,
                interestLevel = InterestLevel.HOT,
                status = LeadStatus.SITE_VISIT_BOOKED,
                notes = "Interested in 20x20 island booth with dual overhead hanging signs and demo counter.",
                timestamp = System.currentTimeMillis() - 7200000
            ),
            Visitor(
                name = "Marcus Brody",
                company = "Nexus BioTech Labs",
                phone = "(555) 782-4411",
                email = "brody@nexusbio.io",
                source = VisitorSource.DIGITAL_CAMPAIGN,
                interestLevel = InterestLevel.WARM,
                status = LeadStatus.ESTIMATE_SENT,
                notes = "Requested pricing for conference backdrop and branded interactive touch display.",
                timestamp = System.currentTimeMillis() - 18000000
            ),
            Visitor(
                name = "Sophia Chen",
                company = "Chen Architecture & Design",
                phone = "(555) 901-3322",
                email = "sophia@chenarch.com",
                source = VisitorSource.REFERRAL,
                interestLevel = InterestLevel.HOT,
                status = LeadStatus.CONVERTED,
                notes = "Client approved custom acrylic dimensional lettering and lobby reception measurements.",
                timestamp = System.currentTimeMillis() - 43200000
            ),
            Visitor(
                name = "David Miller",
                company = "Miller Retail Outlets",
                phone = "(555) 441-9876",
                email = "dmiller@milleroutlets.com",
                source = VisitorSource.STOREFRONT,
                interestLevel = InterestLevel.COLD,
                status = LeadStatus.CONTACTED,
                notes = "Looking for window promotional decal measurements and quote for seasonal rollout.",
                timestamp = System.currentTimeMillis() - 86400000
            )
        )
        _visitors.value = sampleVisitors

        val sampleMeasurements = listOf(
            Measurement(
                locationName = "Convention Center Hall B - Booth #408",
                clientName = "Vance Global Logistics",
                length = 20.0,
                width = 20.0,
                height = 12.0,
                unit = MeasurementUnit.FEET,
                notes = "Corner booth configuration with 2 open sides. Requires interlocking flooring and 12ft truss arch."
            ),
            Measurement(
                locationName = "Nexus BioTech Lobby Reception Wall",
                clientName = "Nexus BioTech Labs",
                length = 24.5,
                width = 1.5,
                height = 10.0,
                unit = MeasurementUnit.FEET,
                notes = "Smooth drywall ready for 3D backlit metal logo and acoustic slat wall panels."
            ),
            Measurement(
                locationName = "Showroom Main Display Floor",
                clientName = "Chen Architecture & Design",
                length = 15.0,
                width = 10.0,
                height = 9.0,
                unit = MeasurementUnit.FEET,
                notes = "Full floor graphic overlay with polyurethane protective coating."
            )
        )
        _measurements.value = sampleMeasurements

        val sampleEstimates = listOf(
            Estimate(
                quoteNumber = "EST-8492",
                clientName = "Nexus BioTech Labs",
                clientContact = "brody@nexusbio.io",
                projectTitle = "Lobby Backlit LED Signage & Touch Kiosk",
                linkedMeasurementId = sampleMeasurements[1].id,
                items = listOf(
                    EstimateItem(title = "Backlit LED Fabric Signage Wall", category = "Signage", quantity = 120.0, unit = "sq ft", unitPrice = 45.0),
                    EstimateItem(title = "55-inch 4K Interactive Touch Kiosk", category = "Digital", quantity = 1.0, unit = "unit", unitPrice = 1250.0),
                    EstimateItem(title = "On-Site Installation & Assembly Team", category = "Labor", quantity = 8.0, unit = "hr", unitPrice = 85.0)
                ),
                taxRatePercent = 8.5,
                markupPercent = 15.0,
                discountPercent = 5.0,
                status = EstimateStatus.SENT,
                notes = "Delivery within 10 business days upon quote acceptance. 50% deposit required."
            ),
            Estimate(
                quoteNumber = "EST-9104",
                clientName = "Chen Architecture & Design",
                clientContact = "sophia@chenarch.com",
                projectTitle = "Showroom Floor Graphic & Retractable Banners",
                linkedMeasurementId = sampleMeasurements[2].id,
                items = listOf(
                    EstimateItem(title = "Interlocking Wood Grain Flooring Tiles", category = "Materials", quantity = 150.0, unit = "sq ft", unitPrice = 6.50),
                    EstimateItem(title = "Retractable Premium Roll-Up Banner", category = "Print", quantity = 2.0, unit = "unit", unitPrice = 165.0),
                    EstimateItem(title = "On-Site Installation & Assembly Team", category = "Labor", quantity = 4.0, unit = "hr", unitPrice = 85.0)
                ),
                taxRatePercent = 8.5,
                markupPercent = 10.0,
                discountPercent = 0.0,
                status = EstimateStatus.ACCEPTED,
                notes = "Approved by client. Installation scheduled for Friday."
            )
        )
        _estimates.value = sampleEstimates

        val sampleGallery = listOf(
            GalleryPhoto(
                title = "Vance Global Expo 20x20 Booth",
                category = PhotoCategory.EXPO_BOOTH,
                clientName = "Vance Global Logistics",
                vectorIconName = "trade_show",
                description = "Modern island booth with custom overhead branding and live product demo stations."
            ),
            GalleryPhoto(
                title = "Nexus BioTech Lobby Facade",
                category = PhotoCategory.INSTALLATION,
                clientName = "Nexus BioTech Labs",
                vectorIconName = "architecture",
                description = "Brushed stainless steel illuminated sign and integrated digital directory."
            ),
            GalleryPhoto(
                title = "Trade Center Flooring Survey",
                category = PhotoCategory.SITE_SURVEY,
                clientName = "Chen Architecture & Design",
                vectorIconName = "square_foot",
                description = "Precise laser measurements of showroom floor before tile installation."
            ),
            GalleryPhoto(
                title = "Product Launch Backdrop & Banners",
                category = PhotoCategory.MARKETING,
                clientName = "Miller Retail Outlets",
                vectorIconName = "campaign",
                description = "High resolution matte step-and-repeat media wall for VIP press event."
            )
        )
        _gallery.value = sampleGallery

        _settings.value = AppSettings()
        _quickVisitorCount.value = 24

        saveToPrefs()
    }

    private fun saveToPrefs() {
        val editor = prefs.edit()
        editor.putBoolean("has_initialized_v2", true)
        editor.putInt("quick_count", _quickVisitorCount.value)

        // Save Settings
        val s = _settings.value
        val settingsObj = JSONObject().apply {
            put("businessName", s.businessName)
            put("businessTagline", s.businessTagline)
            put("phone", s.phone)
            put("email", s.email)
            put("address", s.address)
            put("currencySymbol", s.currencySymbol)
            put("defaultUnit", s.defaultUnit.name)
            put("defaultTaxRate", s.defaultTaxRate)
            put("defaultMarkupRate", s.defaultMarkupRate)
            put("dailyVisitorTarget", s.dailyVisitorTarget)
            put("isDarkMode", s.isDarkMode)
        }
        editor.putString("settings_json", settingsObj.toString())

        // Save Visitors
        val visitorsArray = JSONArray()
        for (v in _visitors.value) {
            val vObj = JSONObject().apply {
                put("id", v.id)
                put("name", v.name)
                put("company", v.company)
                put("phone", v.phone)
                put("email", v.email)
                put("source", v.source.name)
                put("interestLevel", v.interestLevel.name)
                put("status", v.status.name)
                put("notes", v.notes)
                put("timestamp", v.timestamp)
            }
            visitorsArray.put(vObj)
        }
        editor.putString("visitors_json", visitorsArray.toString())

        // Save Measurements
        val measurementsArray = JSONArray()
        for (m in _measurements.value) {
            val mObj = JSONObject().apply {
                put("id", m.id)
                put("locationName", m.locationName)
                put("clientOrVisitorId", m.clientOrVisitorId)
                put("clientName", m.clientName)
                put("length", m.length)
                put("width", m.width)
                put("height", m.height)
                put("unit", m.unit.name)
                put("notes", m.notes)
                if (m.photoUri != null) put("photoUri", m.photoUri)
                put("timestamp", m.timestamp)
            }
            measurementsArray.put(mObj)
        }
        editor.putString("measurements_json", measurementsArray.toString())

        // Save Estimates
        val estimatesArray = JSONArray()
        for (e in _estimates.value) {
            val eObj = JSONObject().apply {
                put("id", e.id)
                put("quoteNumber", e.quoteNumber)
                put("clientName", e.clientName)
                put("clientContact", e.clientContact)
                put("projectTitle", e.projectTitle)
                if (e.linkedMeasurementId != null) put("linkedMeasurementId", e.linkedMeasurementId)
                put("taxRatePercent", e.taxRatePercent)
                put("markupPercent", e.markupPercent)
                put("discountPercent", e.discountPercent)
                put("status", e.status.name)
                put("notes", e.notes)
                put("timestamp", e.timestamp)

                val itemsArray = JSONArray()
                for (it in e.items) {
                    val itObj = JSONObject().apply {
                        put("id", it.id)
                        put("title", it.title)
                        put("category", it.category)
                        put("quantity", it.quantity)
                        put("unit", it.unit)
                        put("unitPrice", it.unitPrice)
                    }
                    itemsArray.put(itObj)
                }
                put("items", itemsArray)
            }
            estimatesArray.put(eObj)
        }
        editor.putString("estimates_json", estimatesArray.toString())

        // Save Catalog
        val catalogArray = JSONArray()
        for (c in _catalog.value) {
            val cObj = JSONObject().apply {
                put("id", c.id)
                put("sku", c.sku)
                put("title", c.title)
                put("category", c.category.name)
                put("unitPrice", c.unitPrice)
                put("unit", c.unit)
                put("description", c.description)
                put("inStock", c.inStock)
                put("tags", JSONArray(c.tags))
            }
            catalogArray.put(cObj)
        }
        editor.putString("catalog_json", catalogArray.toString())

        // Save Gallery
        val galleryArray = JSONArray()
        for (g in _gallery.value) {
            val gObj = JSONObject().apply {
                put("id", g.id)
                put("title", g.title)
                put("category", g.category.name)
                if (g.photoUri != null) put("photoUri", g.photoUri)
                put("vectorIconName", g.vectorIconName)
                put("clientName", g.clientName)
                put("description", g.description)
                put("timestamp", g.timestamp)
            }
            galleryArray.put(gObj)
        }
        editor.putString("gallery_json", galleryArray.toString())

        editor.apply()
    }

    private fun loadFromPrefs() {
        try {
            _quickVisitorCount.value = prefs.getInt("quick_count", 24)

            // Settings
            val settingsStr = prefs.getString("settings_json", null)
            if (settingsStr != null) {
                val obj = JSONObject(settingsStr)
                _settings.value = AppSettings(
                    businessName = obj.optString("businessName", "Apex Marketing & Fabrication"),
                    businessTagline = obj.optString("businessTagline", "Precision Measurements & Estimates"),
                    phone = obj.optString("phone", "+1 (555) 389-4400"),
                    email = obj.optString("email", "contact@apexmarketing.com"),
                    address = obj.optString("address", "100 Innovation Way, Suite 400"),
                    currencySymbol = obj.optString("currencySymbol", "$"),
                    defaultUnit = runCatching { MeasurementUnit.valueOf(obj.optString("defaultUnit")) }.getOrDefault(MeasurementUnit.FEET),
                    defaultTaxRate = obj.optDouble("defaultTaxRate", 8.5),
                    defaultMarkupRate = obj.optDouble("defaultMarkupRate", 15.0),
                    dailyVisitorTarget = obj.optInt("dailyVisitorTarget", 50),
                    isDarkMode = obj.optBoolean("isDarkMode", false)
                )
            }

            // Visitors
            val visitorsStr = prefs.getString("visitors_json", null)
            if (visitorsStr != null) {
                val array = JSONArray(visitorsStr)
                val list = mutableListOf<Visitor>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        Visitor(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            company = obj.optString("company", ""),
                            phone = obj.optString("phone", ""),
                            email = obj.optString("email", ""),
                            source = runCatching { VisitorSource.valueOf(obj.getString("source")) }.getOrDefault(VisitorSource.EXPO_BOOTH),
                            interestLevel = runCatching { InterestLevel.valueOf(obj.getString("interestLevel")) }.getOrDefault(InterestLevel.WARM),
                            status = runCatching { LeadStatus.valueOf(obj.getString("status")) }.getOrDefault(LeadStatus.NEW),
                            notes = obj.optString("notes", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                _visitors.value = list
            }

            // Measurements
            val measurementsStr = prefs.getString("measurements_json", null)
            if (measurementsStr != null) {
                val array = JSONArray(measurementsStr)
                val list = mutableListOf<Measurement>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        Measurement(
                            id = obj.getString("id"),
                            locationName = obj.getString("locationName"),
                            clientOrVisitorId = obj.optString("clientOrVisitorId", ""),
                            clientName = obj.optString("clientName", ""),
                            length = obj.optDouble("length", 10.0),
                            width = obj.optDouble("width", 10.0),
                            height = obj.optDouble("height", 0.0),
                            unit = runCatching { MeasurementUnit.valueOf(obj.getString("unit")) }.getOrDefault(MeasurementUnit.FEET),
                            notes = obj.optString("notes", ""),
                            photoUri = if (obj.has("photoUri")) obj.getString("photoUri") else null,
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                _measurements.value = list
            }

            // Estimates
            val estimatesStr = prefs.getString("estimates_json", null)
            if (estimatesStr != null) {
                val array = JSONArray(estimatesStr)
                val list = mutableListOf<Estimate>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val itemsArray = obj.optJSONArray("items") ?: JSONArray()
                    val itemsList = mutableListOf<EstimateItem>()
                    for (j in 0 until itemsArray.length()) {
                        val itObj = itemsArray.getJSONObject(j)
                        itemsList.add(
                            EstimateItem(
                                id = itObj.optString("id", java.util.UUID.randomUUID().toString()),
                                title = itObj.getString("title"),
                                category = itObj.optString("category", "Materials"),
                                quantity = itObj.optDouble("quantity", 1.0),
                                unit = itObj.optString("unit", "units"),
                                unitPrice = itObj.optDouble("unitPrice", 0.0)
                            )
                        )
                    }
                    list.add(
                        Estimate(
                            id = obj.getString("id"),
                            quoteNumber = obj.getString("quoteNumber"),
                            clientName = obj.getString("clientName"),
                            clientContact = obj.optString("clientContact", ""),
                            projectTitle = obj.getString("projectTitle"),
                            linkedMeasurementId = if (obj.has("linkedMeasurementId")) obj.getString("linkedMeasurementId") else null,
                            items = itemsList,
                            taxRatePercent = obj.optDouble("taxRatePercent", 8.5),
                            markupPercent = obj.optDouble("markupPercent", 15.0),
                            discountPercent = obj.optDouble("discountPercent", 0.0),
                            status = runCatching { EstimateStatus.valueOf(obj.getString("status")) }.getOrDefault(EstimateStatus.DRAFT),
                            notes = obj.optString("notes", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                _estimates.value = list
            }

            // Catalog
            val catalogStr = prefs.getString("catalog_json", null)
            if (catalogStr != null) {
                val array = JSONArray(catalogStr)
                val list = mutableListOf<CatalogItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val tagsArr = obj.optJSONArray("tags") ?: JSONArray()
                    val tags = mutableListOf<String>()
                    for (t in 0 until tagsArr.length()) {
                        tags.add(tagsArr.getString(t))
                    }
                    list.add(
                        CatalogItem(
                            id = obj.getString("id"),
                            sku = obj.getString("sku"),
                            title = obj.getString("title"),
                            category = runCatching { CatalogCategory.valueOf(obj.getString("category")) }.getOrDefault(CatalogCategory.SIGNAGE),
                            unitPrice = obj.optDouble("unitPrice", 0.0),
                            unit = obj.getString("unit"),
                            description = obj.optString("description", ""),
                            inStock = obj.optBoolean("inStock", true),
                            tags = tags
                        )
                    )
                }
                _catalog.value = list
            }

            // Gallery
            val galleryStr = prefs.getString("gallery_json", null)
            if (galleryStr != null) {
                val array = JSONArray(galleryStr)
                val list = mutableListOf<GalleryPhoto>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        GalleryPhoto(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            category = runCatching { PhotoCategory.valueOf(obj.getString("category")) }.getOrDefault(PhotoCategory.EXPO_BOOTH),
                            photoUri = if (obj.has("photoUri")) obj.getString("photoUri") else null,
                            vectorIconName = obj.optString("vectorIconName", "trade_show"),
                            clientName = obj.optString("clientName", ""),
                            description = obj.optString("description", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                _gallery.value = list
            }
        } catch (e: Exception) {
            e.printStackTrace()
            seedSampleData()
        }
    }

    // Visitor Operations
    fun addVisitor(visitor: Visitor) {
        _visitors.value = listOf(visitor) + _visitors.value
        _quickVisitorCount.value = _quickVisitorCount.value + 1
        saveToPrefs()
    }

    fun updateVisitor(updated: Visitor) {
        _visitors.value = _visitors.value.map { if (it.id == updated.id) updated else it }
        saveToPrefs()
    }

    fun deleteVisitor(id: String) {
        _visitors.value = _visitors.value.filterNot { it.id == id }
        saveToPrefs()
    }

    fun incrementQuickVisitorCount() {
        _quickVisitorCount.value = _quickVisitorCount.value + 1
        prefs.edit().putInt("quick_count", _quickVisitorCount.value).apply()
    }

    // Measurement Operations
    fun addMeasurement(measurement: Measurement) {
        _measurements.value = listOf(measurement) + _measurements.value
        saveToPrefs()
    }

    fun updateMeasurement(updated: Measurement) {
        _measurements.value = _measurements.value.map { if (it.id == updated.id) updated else it }
        saveToPrefs()
    }

    fun deleteMeasurement(id: String) {
        _measurements.value = _measurements.value.filterNot { it.id == id }
        saveToPrefs()
    }

    // Estimate Operations
    fun addEstimate(estimate: Estimate) {
        _estimates.value = listOf(estimate) + _estimates.value
        saveToPrefs()
    }

    fun updateEstimate(updated: Estimate) {
        _estimates.value = _estimates.value.map { if (it.id == updated.id) updated else it }
        saveToPrefs()
    }

    fun updateEstimateStatus(id: String, status: EstimateStatus) {
        _estimates.value = _estimates.value.map { if (it.id == id) it.copy(status = status) else it }
        saveToPrefs()
    }

    fun deleteEstimate(id: String) {
        _estimates.value = _estimates.value.filterNot { it.id == id }
        saveToPrefs()
    }

    // Catalog Operations
    fun addCatalogItem(item: CatalogItem) {
        _catalog.value = _catalog.value + item
        saveToPrefs()
    }

    fun updateCatalogItem(updated: CatalogItem) {
        _catalog.value = _catalog.value.map { if (it.id == updated.id) updated else it }
        saveToPrefs()
    }

    fun deleteCatalogItem(id: String) {
        _catalog.value = _catalog.value.filterNot { it.id == id }
        saveToPrefs()
    }

    // Gallery Operations
    fun addPhoto(photo: GalleryPhoto) {
        _gallery.value = listOf(photo) + _gallery.value
        saveToPrefs()
    }

    fun deletePhoto(id: String) {
        _gallery.value = _gallery.value.filterNot { it.id == id }
        saveToPrefs()
    }

    // Settings Operations
    fun updateSettings(settings: AppSettings) {
        _settings.value = settings
        saveToPrefs()
    }

    fun resetToSampleData() {
        prefs.edit().clear().apply()
        seedSampleData()
    }

    fun generateBusinessSummaryReport(): String {
        val s = _settings.value
        val sb = StringBuilder()
        sb.append("📊 ${s.businessName.uppercase(Locale.US)} - BUSINESS SUMMARY REPORT\n")
        sb.append("${s.businessTagline}\n")
        sb.append("Contact: ${s.phone} | ${s.email}\n")
        sb.append("=========================================\n\n")

        sb.append("1. MARKETING & VISITOR MEASUREMENT:\n")
        sb.append("• Quick Footfall Count: ${_quickVisitorCount.value} / ${s.dailyVisitorTarget} target\n")
        sb.append("• Registered Leads: ${_visitors.value.size}\n")
        val hotCount = _visitors.value.count { it.interestLevel == InterestLevel.HOT }
        val closedCount = _visitors.value.count { it.status == LeadStatus.CONVERTED }
        sb.append("• High-Intent (Hot) Leads: $hotCount\n")
        sb.append("• Converted Clients: $closedCount\n\n")

        sb.append("2. ESTIMATES & QUOTES PIPELINE:\n")
        sb.append("• Total Estimates Created: ${_estimates.value.size}\n")
        val totalPipeline = _estimates.value.sumOf { it.grandTotal }
        val wonPipeline = _estimates.value.filter { it.status == EstimateStatus.ACCEPTED || it.status == EstimateStatus.COMPLETED }.sumOf { it.grandTotal }
        sb.append("• Total Pipeline Value: ${s.currencySymbol}${String.format(Locale.US, "%.2f", totalPipeline)}\n")
        sb.append("• Accepted / Won Revenue: ${s.currencySymbol}${String.format(Locale.US, "%.2f", wonPipeline)}\n\n")

        sb.append("3. SITE MEASUREMENTS RECORDED:\n")
        sb.append("• Total Areas Surveyed: ${_measurements.value.size}\n")
        val totalArea = _measurements.value.sumOf { it.floorArea }
        sb.append("• Total Area Measured: ${String.format(Locale.US, "%.1f", totalArea)} ${s.defaultUnit.areaLabel}\n\n")

        sb.append("4. CATALOG & PORTFOLIO:\n")
        sb.append("• Active Catalog Offerings: ${_catalog.value.size}\n")
        sb.append("• Portfolio Showcase Photos: ${_gallery.value.size}\n")

        return sb.toString()
    }
}
