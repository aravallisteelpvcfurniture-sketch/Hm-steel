package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTab(val title: String) {
    DASHBOARD("Dashboard"),
    VISITORS("Visitors & Leads"),
    MEASUREMENTS("Measurements"),
    ESTIMATES("Estimates"),
    CATALOG("Catalog"),
    GALLERY("Gallery"),
    SETTINGS("Menu & Settings")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val repository = AppRepository(application.applicationContext)

    val visitors = repository.visitors
    val measurements = repository.measurements
    val estimates = repository.estimates
    val catalog = repository.catalog
    val gallery = repository.gallery
    val settings = repository.settings
    val quickVisitorCount = repository.quickVisitorCount

    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Deep-link / pre-fill states between screens
    private val _prefilledMeasurementId = MutableStateFlow<String?>(null)
    val prefilledMeasurementId: StateFlow<String?> = _prefilledMeasurementId.asStateFlow()

    private val _prefilledVisitorName = MutableStateFlow<String?>(null)
    val prefilledVisitorName: StateFlow<String?> = _prefilledVisitorName.asStateFlow()

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // Quick tally footfall
    fun incrementVisitorCount() {
        repository.incrementQuickVisitorCount()
    }

    // Visitor actions
    fun addVisitor(visitor: Visitor) {
        repository.addVisitor(visitor)
    }

    fun updateVisitor(visitor: Visitor) {
        repository.updateVisitor(visitor)
    }

    fun deleteVisitor(id: String) {
        repository.deleteVisitor(id)
    }

    // Measurement actions
    fun addMeasurement(measurement: Measurement) {
        repository.addMeasurement(measurement)
    }

    fun updateMeasurement(measurement: Measurement) {
        repository.updateMeasurement(measurement)
    }

    fun deleteMeasurement(id: String) {
        repository.deleteMeasurement(id)
    }

    // Estimate actions
    fun addEstimate(estimate: Estimate) {
        repository.addEstimate(estimate)
    }

    fun updateEstimate(estimate: Estimate) {
        repository.updateEstimate(estimate)
    }

    fun updateEstimateStatus(id: String, status: EstimateStatus) {
        repository.updateEstimateStatus(id, status)
    }

    fun deleteEstimate(id: String) {
        repository.deleteEstimate(id)
    }

    // Catalog actions
    fun addCatalogItem(item: CatalogItem) {
        repository.addCatalogItem(item)
    }

    fun updateCatalogItem(item: CatalogItem) {
        repository.updateCatalogItem(item)
    }

    fun deleteCatalogItem(id: String) {
        repository.deleteCatalogItem(id)
    }

    // Gallery actions
    fun addPhoto(photo: GalleryPhoto) {
        repository.addPhoto(photo)
    }

    fun deletePhoto(id: String) {
        repository.deletePhoto(id)
    }

    // Settings actions
    fun updateSettings(newSettings: AppSettings) {
        repository.updateSettings(newSettings)
    }

    fun resetToSampleData() {
        repository.resetToSampleData()
    }

    fun getSummaryReport(): String {
        return repository.generateBusinessSummaryReport()
    }

    // Workflow navigators
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
