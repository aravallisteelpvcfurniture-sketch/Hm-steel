package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.MainViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstimateScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val estimates by viewModel.estimates.collectAsState()
    val measurements by viewModel.measurements.collectAsState()
    val catalog by viewModel.catalog.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val prefilledMeasurementId by viewModel.prefilledMeasurementId.collectAsState()
    val prefilledVisitorName by viewModel.prefilledVisitorName.collectAsState()

    var selectedStatusFilter by remember { mutableStateOf<EstimateStatus?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    // If pre-filled data came in, open dialog automatically
    LaunchedEffect(prefilledMeasurementId, prefilledVisitorName) {
        if (prefilledMeasurementId != null || prefilledVisitorName != null) {
            showCreateDialog = true
        }
    }

    val filteredEstimates = estimates.filter {
        selectedStatusFilter == null || it.status == selectedStatusFilter
    }

    val totalPipeline = estimates.sumOf { it.grandTotal }
    val wonPipeline = estimates.filter { it.status == EstimateStatus.ACCEPTED || it.status == EstimateStatus.COMPLETED }.sumOf { it.grandTotal }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("estimates_screen"),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.AddCircle, contentDescription = "New Estimate") },
                text = { Text("New Estimate") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("create_estimate_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Financial Summary Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Active Pipeline Value", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        Text(
                            "${settings.currencySymbol}${String.format(Locale.US, "%.2f", totalPipeline)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Won / Accepted", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        Text(
                            "${settings.currencySymbol}${String.format(Locale.US, "%.2f", wonPipeline)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status filter chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null },
                        label = { Text("All (${estimates.size})") }
                    )
                }
                items(EstimateStatus.values()) { status ->
                    val count = estimates.count { it.status == status }
                    FilterChip(
                        selected = selectedStatusFilter == status,
                        onClick = { selectedStatusFilter = if (selectedStatusFilter == status) null else status },
                        label = { Text("${status.label} ($count)") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredEstimates.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No estimates found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Create client quotations with itemized costs, tax, and markup.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredEstimates, key = { it.id }) { estimate ->
                        EstimateCard(
                            estimate = estimate,
                            currencySymbol = settings.currencySymbol,
                            onStatusChange = { newStatus -> viewModel.updateEstimateStatus(estimate.id, newStatus) },
                            onDelete = { viewModel.deleteEstimate(estimate.id) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateEstimateDialog(
            measurements = measurements,
            catalog = catalog,
            settings = settings,
            initialMeasurementId = prefilledMeasurementId,
            initialClientName = prefilledVisitorName ?: "",
            onDismiss = {
                showCreateDialog = false
                viewModel.clearPrefilledData()
            },
            onSave = { newEstimate ->
                viewModel.addEstimate(newEstimate)
                showCreateDialog = false
                viewModel.clearPrefilledData()
            }
        )
    }
}

@Composable
fun EstimateCard(
    estimate: Estimate,
    currencySymbol: String,
    onStatusChange: (EstimateStatus) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = estimate.quoteNumber,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = estimate.projectTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Client: ${estimate.clientName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusPill(status = estimate.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Financial summary line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${estimate.items.size} line items · ${estimate.formattedDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$currencySymbol${String.format(Locale.US, "%.2f", estimate.grandTotal)}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
            }

            // Expandable details
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Line Items Breakdown:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))

                    estimate.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.title} (${item.quantity} ${item.unit})",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$currencySymbol${String.format(Locale.US, "%.2f", item.total)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", style = MaterialTheme.typography.bodySmall)
                        Text("$currencySymbol${String.format(Locale.US, "%.2f", estimate.subtotal)}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (estimate.markupPercent > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Markup (${estimate.markupPercent}%)", style = MaterialTheme.typography.bodySmall)
                            Text("+$currencySymbol${String.format(Locale.US, "%.2f", estimate.markupAmount)}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tax (${estimate.taxRatePercent}%)", style = MaterialTheme.typography.bodySmall)
                        Text("+$currencySymbol${String.format(Locale.US, "%.2f", estimate.taxAmount)}", style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Update Status:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                        items(EstimateStatus.values()) { st ->
                            FilterChip(
                                selected = estimate.status == st,
                                onClick = { onStatusChange(st) },
                                label = { Text(st.label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { isExpanded = !isExpanded }) {
                    Text(if (isExpanded) "Hide Details" else "View Details")
                    Icon(if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }

                    IconButton(onClick = {
                        val shareText = estimate.toShareableText(currencySymbol)
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Estimate ${estimate.quoteNumber}")
                        context.startActivity(shareIntent)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
                    }

                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Estimate", estimate.toShareableText(currencySymbol))
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Estimate copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEstimateDialog(
    measurements: List<Measurement>,
    catalog: List<CatalogItem>,
    settings: AppSettings,
    initialMeasurementId: String?,
    initialClientName: String,
    onDismiss: () -> Unit,
    onSave: (Estimate) -> Unit
) {
    var projectTitle by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf(initialClientName) }
    var clientContact by remember { mutableStateOf("") }
    var selectedMeasurementId by remember { mutableStateOf(initialMeasurementId) }
    var taxRate by remember { mutableStateOf(settings.defaultTaxRate.toString()) }
    var markupRate by remember { mutableStateOf(settings.defaultMarkupRate.toString()) }
    var discountRate by remember { mutableStateOf("0.0") }
    var notes by remember { mutableStateOf("Valid for 30 days. Includes delivery & installation.") }

    var itemsList by remember {
        val initialList = mutableListOf<EstimateItem>()
        // Auto populate with matching measurement if available
        val matched = measurements.firstOrNull { it.id == initialMeasurementId }
        if (matched != null) {
            initialList.add(
                EstimateItem(
                    title = "Site Area Setup (${matched.locationName})",
                    category = "Materials",
                    quantity = matched.floorArea,
                    unit = matched.unit.areaLabel,
                    unitPrice = 12.0
                )
            )
        }
        mutableStateOf(initialList.toList())
    }

    var showCatalogPicker by remember { mutableStateOf(false) }

    // Custom new item inputs
    var newItemTitle by remember { mutableStateOf("") }
    var newItemPrice by remember { mutableStateOf("100.0") }
    var newItemQty by remember { mutableStateOf("1.0") }
    var newItemUnit by remember { mutableStateOf("unit") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Draft New Quote / Estimate", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = projectTitle,
                        onValueChange = { projectTitle = it },
                        label = { Text("Project Title *") },
                        placeholder = { Text("e.g. Expo 20x20 Booth Build") },
                        modifier = Modifier.fillMaxWidth().testTag("estimate_title_input"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Client / Company Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("estimate_client_input"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = clientContact,
                        onValueChange = { clientContact = it },
                        label = { Text("Client Phone / Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Measurement Linking (optional)
                if (measurements.isNotEmpty()) {
                    item {
                        Text("Link to Site Measurement (Optional):", style = MaterialTheme.typography.labelMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedMeasurementId == null,
                                    onClick = { selectedMeasurementId = null },
                                    label = { Text("None") }
                                )
                            }
                            items(measurements) { m ->
                                FilterChip(
                                    selected = selectedMeasurementId == m.id,
                                    onClick = { selectedMeasurementId = m.id },
                                    label = { Text("${m.locationName} (${String.format(Locale.US, "%.0f", m.floorArea)} ${m.unit.areaLabel})") }
                                )
                            }
                        }
                    }
                }

                // Line items section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Quote Line Items (${itemsList.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        OutlinedButton(
                            onClick = { showCatalogPicker = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Catalog Item", fontSize = 11.sp)
                        }
                    }
                }

                items(itemsList) { itm ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(itm.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("${itm.quantity} ${itm.unit} @ ${settings.currencySymbol}${itm.unitPrice} = ${settings.currencySymbol}${String.format(Locale.US, "%.2f", itm.total)}", fontSize = 11.sp)
                            }
                            IconButton(onClick = { itemsList = itemsList.filterNot { it.id == itm.id } }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Quick custom item input
                item {
                    Text("Add Custom Item:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = newItemTitle,
                        onValueChange = { newItemTitle = it },
                        label = { Text("Item Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = newItemPrice,
                            onValueChange = { newItemPrice = it },
                            label = { Text("Price") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newItemQty,
                            onValueChange = { newItemQty = it },
                            label = { Text("Qty") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                val price = newItemPrice.toDoubleOrNull() ?: 0.0
                                val qty = newItemQty.toDoubleOrNull() ?: 1.0
                                if (newItemTitle.isNotBlank() && price > 0) {
                                    itemsList = itemsList + EstimateItem(
                                        title = newItemTitle.trim(),
                                        category = "Custom",
                                        quantity = qty,
                                        unit = newItemUnit,
                                        unitPrice = price
                                    )
                                    newItemTitle = ""
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterVertically),
                            enabled = newItemTitle.isNotBlank()
                        ) {
                            Text("Add")
                        }
                    }
                }

                // Rates: Tax, Markup, Discount
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = markupRate,
                            onValueChange = { markupRate = it },
                            label = { Text("Markup %") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = taxRate,
                            onValueChange = { taxRate = it },
                            label = { Text("Tax %") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = discountRate,
                            onValueChange = { discountRate = it },
                            label = { Text("Discount %") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Proposal Notes / Terms") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (projectTitle.isNotBlank() && clientName.isNotBlank()) {
                        val est = Estimate(
                            projectTitle = projectTitle.trim(),
                            clientName = clientName.trim(),
                            clientContact = clientContact.trim(),
                            linkedMeasurementId = selectedMeasurementId,
                            items = itemsList,
                            taxRatePercent = taxRate.toDoubleOrNull() ?: 0.0,
                            markupPercent = markupRate.toDoubleOrNull() ?: 0.0,
                            discountPercent = discountRate.toDoubleOrNull() ?: 0.0,
                            notes = notes.trim(),
                            status = EstimateStatus.DRAFT
                        )
                        onSave(est)
                    }
                },
                enabled = projectTitle.isNotBlank() && clientName.isNotBlank(),
                modifier = Modifier.testTag("save_estimate_btn")
            ) {
                Text("Generate Estimate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // Catalog Item Picker Modal
    if (showCatalogPicker) {
        AlertDialog(
            onDismissRequest = { showCatalogPicker = false },
            title = { Text("Select from Catalog", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(catalog) { catItem ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                itemsList = itemsList + EstimateItem(
                                    title = catItem.title,
                                    category = catItem.category.label,
                                    quantity = 1.0,
                                    unit = catItem.unit,
                                    unitPrice = catItem.unitPrice
                                )
                                showCatalogPicker = false
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(catItem.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${catItem.category.label} · ${settings.currencySymbol}${catItem.unitPrice} / ${catItem.unit}", fontSize = 11.sp)
                                }
                                Icon(Icons.Default.AddCircleOutline, contentDescription = "Add")
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCatalogPicker = false }) {
                    Text("Close")
                }
            }
        )
    }
}
