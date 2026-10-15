package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.util.Locale
import java.util.UUID

class AppRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    // Secondary constructor resolving named database ID from string resources
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            FirebaseApp.getInstance(),
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- VISITORS ---
    fun observeVisitors(userId: String): Flow<List<Visitor>> = callbackFlow {
        val path = "users/$userId/visitors"
        val ref = db.collection("users").document(userId).collection("visitors")
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, path)
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.toObjects(Visitor::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: emptyList()
            trySend(items)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addVisitor(visitor: Visitor): String {
        val uid = requireUserId()
        val docId = if (visitor.id.isNotBlank()) visitor.id else UUID.randomUUID().toString()
        val docRef = db.collection("users").document(uid).collection("visitors").document(docId)
        val payload = mapOf(
            "id" to docId,
            "name" to visitor.name,
            "company" to visitor.company,
            "phone" to visitor.phone,
            "email" to visitor.email,
            "source" to visitor.sourceName,
            "interestLevel" to visitor.interestLevelName,
            "status" to visitor.statusName,
            "notes" to visitor.notes,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        try {
            docRef.set(payload).await()
            return docId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateVisitor(visitor: Visitor) {
        val uid = requireUserId()
        require(visitor.id.isNotBlank()) { "Visitor ID must not be blank" }
        val docRef = db.collection("users").document(uid).collection("visitors").document(visitor.id)
        val payload = mapOf(
            "name" to visitor.name,
            "company" to visitor.company,
            "phone" to visitor.phone,
            "email" to visitor.email,
            "source" to visitor.sourceName,
            "interestLevel" to visitor.interestLevelName,
            "status" to visitor.statusName,
            "notes" to visitor.notes,
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        try {
            docRef.update(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteVisitor(visitorId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("visitors").document(visitorId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- MEASUREMENTS ---
    fun observeMeasurements(userId: String): Flow<List<Measurement>> = callbackFlow {
        val path = "users/$userId/measurements"
        val ref = db.collection("users").document(userId).collection("measurements")
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, path)
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.toObjects(Measurement::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: emptyList()
            trySend(items)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addMeasurement(measurement: Measurement): String {
        val uid = requireUserId()
        val docId = if (measurement.id.isNotBlank()) measurement.id else UUID.randomUUID().toString()
        val docRef = db.collection("users").document(uid).collection("measurements").document(docId)
        val payload = mapOf(
            "id" to docId,
            "locationName" to measurement.locationName,
            "clientName" to measurement.clientName,
            "length" to measurement.length,
            "width" to measurement.width,
            "height" to measurement.height,
            "unit" to measurement.unitCode,
            "notes" to measurement.notes,
            "photoUri" to measurement.photoUri,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        try {
            docRef.set(payload).await()
            return docId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteMeasurement(measurementId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("measurements").document(measurementId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- ESTIMATES ---
    fun observeEstimates(userId: String): Flow<List<Estimate>> = callbackFlow {
        val path = "users/$userId/estimates"
        val ref = db.collection("users").document(userId).collection("estimates")
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, path)
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.toObjects(Estimate::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: emptyList()
            trySend(items)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addEstimate(estimate: Estimate): String {
        val uid = requireUserId()
        val docId = if (estimate.id.isNotBlank()) estimate.id else UUID.randomUUID().toString()
        val docRef = db.collection("users").document(uid).collection("estimates").document(docId)
        val payload = mapOf(
            "id" to docId,
            "quoteNumber" to estimate.quoteNumber,
            "clientName" to estimate.clientName,
            "clientContact" to estimate.clientContact,
            "projectTitle" to estimate.projectTitle,
            "linkedMeasurementId" to estimate.linkedMeasurementId,
            "taxRatePercent" to estimate.taxRatePercent,
            "markupPercent" to estimate.markupPercent,
            "discountPercent" to estimate.discountPercent,
            "status" to estimate.statusName,
            "notes" to estimate.notes,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        try {
            docRef.set(payload).await()
            return docId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateEstimateStatus(estimateId: String, status: EstimateStatus) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("estimates").document(estimateId)
        try {
            docRef.update(
                mapOf(
                    "status" to status.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteEstimate(estimateId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("estimates").document(estimateId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- CATALOG ---
    fun observeCatalog(userId: String): Flow<List<CatalogItem>> = callbackFlow {
        val path = "users/$userId/catalog"
        val ref = db.collection("users").document(userId).collection("catalog")
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, path)
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.toObjects(CatalogItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: emptyList()
            trySend(items)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addCatalogItem(item: CatalogItem): String {
        val uid = requireUserId()
        val docId = if (item.id.isNotBlank()) item.id else UUID.randomUUID().toString()
        val docRef = db.collection("users").document(uid).collection("catalog").document(docId)
        val payload = mapOf(
            "id" to docId,
            "sku" to item.sku,
            "title" to item.title,
            "category" to item.categoryName,
            "unitPrice" to item.unitPrice,
            "unit" to item.unit,
            "description" to item.description,
            "inStock" to item.inStock,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        try {
            docRef.set(payload).await()
            return docId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteCatalogItem(itemId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("catalog").document(itemId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- USER SETTINGS ---
    fun observeSettings(userId: String): Flow<AppSettings> = callbackFlow {
        val path = "users/$userId/profile/settings"
        val docRef = db.collection("users").document(userId).collection("profile").document("settings")
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, path)
                close(error)
                return@addSnapshotListener
            }
            val s = snapshot?.toObject(AppSettings::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            trySend(s ?: AppSettings())
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateSettings(settings: AppSettings) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("profile").document("settings")
        val payload = mapOf(
            "businessName" to settings.businessName,
            "businessTagline" to settings.businessTagline,
            "phone" to settings.phone,
            "email" to settings.email,
            "address" to settings.address,
            "currencySymbol" to settings.currencySymbol,
            "defaultUnit" to settings.defaultUnitName,
            "defaultTaxRate" to settings.defaultTaxRate,
            "defaultMarkupRate" to settings.defaultMarkupRate,
            "dailyVisitorTarget" to settings.dailyVisitorTarget,
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        try {
            docRef.set(payload, SetOptions.merge()).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun seedInitialDataIfEmpty(userId: String) {
        try {
            val visitorsSnap = db.collection("users").document(userId).collection("visitors").limit(1).get().await()
            if (visitorsSnap.isEmpty) {
                addVisitor(
                    Visitor(
                        name = "Eleanor Vance",
                        company = "Vance Global Logistics",
                        phone = "(555) 234-8901",
                        email = "e.vance@vancelogistics.com",
                        sourceName = VisitorSource.EXPO_BOOTH.name,
                        interestLevelName = InterestLevel.HOT.name,
                        statusName = LeadStatus.SITE_VISIT_BOOKED.name,
                        notes = "Interested in 20x20 island booth with dual overhead hanging signs and demo counter."
                    )
                )
                addVisitor(
                    Visitor(
                        name = "Marcus Brody",
                        company = "Nexus BioTech Labs",
                        phone = "(555) 782-4411",
                        email = "brody@nexusbio.io",
                        sourceName = VisitorSource.DIGITAL_CAMPAIGN.name,
                        interestLevelName = InterestLevel.WARM.name,
                        statusName = LeadStatus.ESTIMATE_SENT.name,
                        notes = "Requested pricing for conference backdrop and branded interactive touch display."
                    )
                )
            }

            val catalogSnap = db.collection("users").document(userId).collection("catalog").limit(1).get().await()
            if (catalogSnap.isEmpty) {
                addCatalogItem(
                    CatalogItem(
                        sku = "SIG-LED-01",
                        title = "Backlit LED Fabric Signage Wall",
                        categoryName = CatalogCategory.SIGNAGE.name,
                        unitPrice = 45.0,
                        unit = "sq ft",
                        description = "Seamless edge-lit aluminum frame with tension fabric graphic print. High brightness."
                    )
                )
                addCatalogItem(
                    CatalogItem(
                        sku = "BTH-MOD-10",
                        title = "10x10 Modular Truss Booth System",
                        categoryName = CatalogCategory.BOOTH_FABRICATION.name,
                        unitPrice = 3200.0,
                        unit = "unit",
                        description = "Lightweight steel/aluminum truss system with monitor mounts, podium & spot lighting."
                    )
                )
            }
        } catch (e: Exception) {
            Log.w("AppRepository", "Seed initial data skipped: ${e.message}")
        }
    }
}
