package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CatalogCategory
import com.example.data.model.CatalogItem
import com.example.data.model.Estimate
import com.example.data.model.EstimateItem
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: MainViewModel,
    onNavigate: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val catalog by viewModel.catalog.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CatalogCategory?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<CatalogItem?>(null) }

    val filteredItems = catalog.filter { item ->
        val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) ||
                item.sku.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true) ||
                item.tags.any { it.contains(searchQuery, ignoreCase = true) }
        val matchesCategory = selectedCategory == null || item.category == selectedCategory
        matchesSearch && matchesCategory
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("catalog_screen"),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Item") },
                text = { Text("New Offering") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_catalog_fab")
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

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().testTag("catalog_search_field"),
                placeholder = { Text("Search catalog by SKU, name, tags...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All (${catalog.size})") }
                    )
                }
                items(CatalogCategory.values()) { cat ->
                    val count = catalog.count { it.category == cat }
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                        label = { Text("${cat.label} ($count)") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No catalog offerings match your criteria.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        CatalogCard(
                            item = item,
                            currencySymbol = settings.currencySymbol,
                            onEdit = { editingItem = item },
                            onDelete = { viewModel.deleteCatalogItem(item.id) },
                            onQuickQuote = {
                                val quickEstimate = Estimate(
                                    projectTitle = "${item.title} Order",
                                    clientName = "Catalog Inquirer",
                                    items = listOf(
                                        EstimateItem(
                                            title = item.title,
                                            category = item.category.label,
                                            quantity = 1.0,
                                            unit = item.unit,
                                            unitPrice = item.unitPrice
                                        )
                                    )
                                )
                                viewModel.addEstimate(quickEstimate)
                                onNavigate(AppTab.ESTIMATES)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CatalogItemDialog(
            title = "Add Catalog Offering",
            initialItem = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { newItem ->
                viewModel.addCatalogItem(newItem)
                showAddDialog = false
            }
        )
    }

    if (editingItem != null) {
        CatalogItemDialog(
            title = "Edit Catalog Item",
            initialItem = editingItem,
            onDismiss = { editingItem = null },
            onConfirm = { updated ->
                viewModel.updateCatalogItem(updated)
                editingItem = null
            }
        )
    }
}

@Composable
fun CatalogCard(
    item: CatalogItem,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onQuickQuote: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SKU chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(text = item.sku, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (item.inStock) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (item.inStock) "Available" else "Backordered",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.inStock) Color(0xFF15803D) else Color(0xFFDC2626)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = item.category.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (item.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Unit Price", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%.2f", item.unitPrice)} / ${item.unit}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                }

                Button(
                    onClick = onQuickQuote,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Draft Quote", fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogItemDialog(
    title: String,
    initialItem: CatalogItem?,
    onDismiss: () -> Unit,
    onConfirm: (CatalogItem) -> Unit
) {
    var titleInput by remember { mutableStateOf(initialItem?.title ?: "") }
    var skuInput by remember { mutableStateOf(initialItem?.sku ?: "ITM-" + (100 + (System.currentTimeMillis() % 900))) }
    var category by remember { mutableStateOf(initialItem?.category ?: CatalogCategory.SIGNAGE) }
    var priceInput by remember { mutableStateOf(initialItem?.unitPrice?.toString() ?: "50.0") }
    var unitInput by remember { mutableStateOf(initialItem?.unit ?: "unit") }
    var description by remember { mutableStateOf(initialItem?.description ?: "") }
    var inStock by remember { mutableStateOf(initialItem?.inStock ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Product / Service Title *") },
                        modifier = Modifier.fillMaxWidth().testTag("catalog_item_title"),
                        singleLine = true
                    )
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = skuInput,
                            onValueChange = { skuInput = it },
                            label = { Text("SKU / Code") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = unitInput,
                            onValueChange = { unitInput = it },
                            label = { Text("Unit (sq ft, hr, pc)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("Base Unit Price ($)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
                item {
                    Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(CatalogCategory.values()) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat.label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = inStock, onCheckedChange = { inStock = it })
                        Text("Active & In-Stock for Quotations", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Specifications & Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceInput.toDoubleOrNull() ?: 0.0
                    if (titleInput.isNotBlank()) {
                        val item = initialItem?.copy(
                            title = titleInput.trim(),
                            sku = skuInput.trim(),
                            category = category,
                            unitPrice = price,
                            unit = unitInput.trim(),
                            description = description.trim(),
                            inStock = inStock
                        ) ?: CatalogItem(
                            title = titleInput.trim(),
                            sku = skuInput.trim(),
                            category = category,
                            unitPrice = price,
                            unit = unitInput.trim(),
                            description = description.trim(),
                            inStock = inStock
                        )
                        onConfirm(item)
                    }
                },
                enabled = titleInput.isNotBlank(),
                modifier = Modifier.testTag("save_catalog_item_btn")
            ) {
                Text("Save Offering")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
