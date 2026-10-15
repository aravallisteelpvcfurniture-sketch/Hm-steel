package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    DASHBOARD("Dashboard"),
    VISITORS("Visitors & Leads"),
    MEASUREMENTS("Measurements"),
    ESTIMATES("Estimates"),
    CATALOG("Catalog"),
    GALLERY("Gallery"),
    SETTINGS("Menu & Settings")
}

class MainViewModel(
    val repository: AppRepository,
    val currentUserId: String
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty(currentUserId)
        }
    }

    val visitors: StateFlow<List<Visitor>> = repository.observeVisitors(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val measurements: StateFlow<List<Measurement>> = repository.observeMeasurements(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val estimates: StateFlow<List<Estimate>> = repository.observeEstimates(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val catalog: StateFlow<List<CatalogItem>> = repository.observeCatalog(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val settings: StateFlow<AppSettings> = repository.observeSettings(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), AppSettings())

    private val _gallery = MutableStateFlow<List<GalleryPhoto>>(
        listOf(
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
    )
    val gallery: StateFlow<List<GalleryPhoto>> = _gallery.asStateFlow()

    private val _quickVisitorCount = MutableStateFlow(24)
    val quickVisitorCount: StateFlow<Int> = _quickVisitorCount.asStateFlow()

    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _prefilledMeasurementId = MutableStateFlow<String?>(null)
    val prefilledMeasurementId: StateFlow<String?> = _prefilledMeasurementId.asStateFlow()

    private val _prefilledVisitorName = MutableStateFlow<String?>(null)
    val prefilledVisitorName: StateFlow<String?> = _prefilledVisitorName.asStateFlow()

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun incrementVisitorCount() {
        _quickVisitorCount.value = _quickVisitorCount.value + 1
    }

    // Visitor actions
    fun addVisitor(visitor: Visitor) {
        viewModelScope.launch {
            repository.addVisitor(visitor)
            incrementVisitorCount()
        }
    }

    fun updateVisitor(visitor: Visitor) {
        viewModelScope.launch {
            repository.updateVisitor(visitor)
        }
    }

    fun deleteVisitor(id: String) {
        viewModelScope.launch {
            repository.deleteVisitor(id)
        }
    }

    // Measurement actions
    fun addMeasurement(measurement: Measurement) {
        viewModelScope.launch {
            repository.addMeasurement(measurement)
        }
    }

    fun deleteMeasurement(id: String) {
        viewModelScope.launch {
            repository.deleteMeasurement(id)
        }
    }

    // Estimate actions
    fun addEstimate(estimate: Estimate) {
        viewModelScope.launch {
            repository.addEstimate(estimate)
        }
    }

    fun updateEstimateStatus(id: String, status: EstimateStatus) {
        viewModelScope.launch {
            repository.updateEstimateStatus(id, status)
        }
    }

    fun deleteEstimate(id: String) {
        viewModelScope.launch {
            repository.deleteEstimate(id)
        }
    }

    // Catalog actions
    fun addCatalogItem(item: CatalogItem) {
        viewModelScope.launch {
            repository.addCatalogItem(item)
        }
    }

    fun deleteCatalogItem(id: String) {
        viewModelScope.launch {
            repository.deleteCatalogItem(id)
        }
    }

    // Gallery actions
    fun addPhoto(photo: GalleryPhoto) {
        _gallery.value = listOf(photo) + _gallery.value
    }

    fun deletePhoto(id: String) {
        _gallery.value = _gallery.value.filterNot { it.id == id }
    }

    // Settings actions
    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            repository.updateSettings(newSettings)
        }
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty(currentUserId)
        }
    }

    fun getSummaryReport(): String {
        val s = settings.value
        val sb = StringBuilder()
        sb.append("📊 ${s.businessName.uppercase(java.util.Locale.US)} - BUSINESS SUMMARY REPORT\n")
        sb.append("${s.businessTagline}\n")
        sb.append("Contact: ${s.phone} | ${s.email}\n")
        sb.append("=========================================\n\n")

        sb.append("1. MARKETING & VISITOR MEASUREMENT:\n")
        sb.append("• Quick Footfall Count: ${_quickVisitorCount.value} / ${s.dailyVisitorTarget} target\n")
        sb.append("• Registered Leads: ${visitors.value.size}\n")
        val hotCount = visitors.value.count { it.interestLevel == InterestLevel.HOT }
        val closedCount = visitors.value.count { it.status == LeadStatus.CONVERTED }
        sb.append("• High-Intent (Hot) Leads: $hotCount\n")
        sb.append("• Converted Clients: $closedCount\n\n")

        sb.append("2. ESTIMATES & QUOTES PIPELINE:\n")
        sb.append("• Total Estimates Created: ${estimates.value.size}\n")
        val totalPipeline = estimates.value.sumOf { it.grandTotal }
        val wonPipeline = estimates.value.filter { it.status == EstimateStatus.ACCEPTED || it.status == EstimateStatus.COMPLETED }.sumOf { it.grandTotal }
        sb.append("• Total Pipeline Value: ${s.currencySymbol}${String.format(java.util.Locale.US, "%.2f", totalPipeline)}\n")
        sb.append("• Accepted / Won Revenue: ${s.currencySymbol}${String.format(java.util.Locale.US, "%.2f", wonPipeline)}\n\n")

        sb.append("3. SITE MEASUREMENTS RECORDED:\n")
        sb.append("• Total Areas Surveyed: ${measurements.value.size}\n")
        val totalArea = measurements.value.sumOf { it.floorArea }
        sb.append("• Total Area Measured: ${String.format(java.util.Locale.US, "%.1f", totalArea)} ${s.defaultUnit.areaLabel}\n\n")

        sb.append("4. CATALOG & PORTFOLIO:\n")
        sb.append("• Active Catalog Offerings: ${catalog.value.size}\n")
        sb.append("• Portfolio Showcase Photos: ${_gallery.value.size}\n")

        return sb.toString()
    }

    fun startEstimateForVisitor(visitor: Visitor) {
        _prefilledVisitorName.value = visitor.name
        _currentTab.value = AppTab.ESTIMATES
    }

    fun startEstimateFromMeasurement(measurement: Measurement) {
        _prefilledMeasurementId.value = measurement.id
        _prefilledVisitorName.value = measurement.clientName
        _currentTab.value = AppTab.ESTIMATES
    }

    fun clearPrefilledData() {
        _prefilledMeasurementId.value = null
        _prefilledVisitorName.value = null
    }
}
