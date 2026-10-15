package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitorsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val visitors by viewModel.visitors.collectAsState()
    val quickCount by viewModel.quickVisitorCount.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSourceFilter by remember { mutableStateOf<VisitorSource?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingVisitor by remember { mutableStateOf<Visitor?>(null) }

    val filteredVisitors = visitors.filter { visitor ->
        val matchesSearch = visitor.name.contains(searchQuery, ignoreCase = true) ||
                visitor.company.contains(searchQuery, ignoreCase = true) ||
                visitor.notes.contains(searchQuery, ignoreCase = true)
        val matchesSource = selectedSourceFilter == null || visitor.source == selectedSourceFilter
        matchesSearch && matchesSource
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("visitors_screen"),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Add Visitor") },
                text = { Text("Log Visitor") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_visitor_fab")
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

            // Quick Traffic Counter Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Booth / Store Footfall Tally",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$quickCount visitors measured today (Target: ${settings.dailyVisitorTarget})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalIconButton(
                            onClick = { viewModel.incrementVisitorCount() },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("tally_plus_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tally +1")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().testTag("visitor_search_field"),
                placeholder = { Text("Search by name, company, or note...") },
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

            // Source Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedSourceFilter == null,
                        onClick = { selectedSourceFilter = null },
                        label = { Text("All (${visitors.size})") }
                    )
                }
                items(VisitorSource.values()) { source ->
                    val count = visitors.count { it.source == source }
                    FilterChip(
                        selected = selectedSourceFilter == source,
                        onClick = { selectedSourceFilter = if (selectedSourceFilter == source) null else source },
                        label = { Text("${source.label} ($count)") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredVisitors.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PeopleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No visitors found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap '+ Log Visitor' to record visitor interactions and inquiries.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredVisitors, key = { it.id }) { visitor ->
                        VisitorCard(
                            visitor = visitor,
                            onEdit = { editingVisitor = visitor },
                            onDelete = { viewModel.deleteVisitor(visitor.id) },
                            onCreateEstimate = { viewModel.startEstimateForVisitor(visitor) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        VisitorDialog(
            title = "Log New Visitor / Inquiry",
            initialVisitor = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { newVisitor ->
                viewModel.addVisitor(newVisitor)
                showAddDialog = false
            }
        )
    }

    if (editingVisitor != null) {
        VisitorDialog(
            title = "Edit Visitor Details",
            initialVisitor = editingVisitor,
            onDismiss = { editingVisitor = null },
            onConfirm = { updated ->
                viewModel.updateVisitor(updated)
                editingVisitor = null
            }
        )
    }
}

@Composable
fun VisitorCard(
    visitor: Visitor,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCreateEstimate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = visitor.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (visitor.company.isNotBlank()) {
                        Text(
                            text = visitor.company,
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                        )
                    }
                }

                // Interest Level badge
                InterestLevelBadge(level = visitor.interestLevel)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Metadata row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (visitor.phone.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(visitor.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (visitor.email.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(visitor.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (visitor.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = visitor.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Source and Status
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text(visitor.source.label, fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(12.dp)) }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(visitor.status.label, fontSize = 11.sp) }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                    Button(
                        onClick = onCreateEstimate,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Quote", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun InterestLevelBadge(level: InterestLevel) {
    val (bg, fg) = when (level) {
        InterestLevel.HOT -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
        InterestLevel.WARM -> Color(0xFFFEF3C7) to Color(0xFFD97706)
        InterestLevel.COLD -> Color(0xFFE2E8F0) to Color(0xFF475569)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = level.label, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitorDialog(
    title: String,
    initialVisitor: Visitor?,
    onDismiss: () -> Unit,
    onConfirm: (Visitor) -> Unit
) {
    var name by remember { mutableStateOf(initialVisitor?.name ?: "") }
    var company by remember { mutableStateOf(initialVisitor?.company ?: "") }
    var phone by remember { mutableStateOf(initialVisitor?.phone ?: "") }
    var email by remember { mutableStateOf(initialVisitor?.email ?: "") }
    var source by remember { mutableStateOf(initialVisitor?.source ?: VisitorSource.EXPO_BOOTH) }
    var interestLevel by remember { mutableStateOf(initialVisitor?.interestLevel ?: InterestLevel.WARM) }
    var status by remember { mutableStateOf(initialVisitor?.status ?: LeadStatus.NEW) }
    var notes by remember { mutableStateOf(initialVisitor?.notes ?: "") }

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
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Visitor / Contact Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("visitor_name_input"),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("Company / Organization") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
                item {
                    Text("Source / Channel", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(VisitorSource.values()) { s ->
                            FilterChip(
                                selected = source == s,
                                onClick = { source = s },
                                label = { Text(s.label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
                item {
                    Text("Interest Intent", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        InterestLevel.values().forEach { level ->
                            FilterChip(
                                selected = interestLevel == level,
                                onClick = { interestLevel = level },
                                label = { Text(level.label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
                item {
                    Text("Lead Status", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(LeadStatus.values()) { st ->
                            FilterChip(
                                selected = status == st,
                                onClick = { status = st },
                                label = { Text(st.label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Inquiry Notes & Requirements") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val visitor = initialVisitor?.copy(
                            name = name.trim(),
                            company = company.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            source = source,
                            interestLevel = interestLevel,
                            status = status,
                            notes = notes.trim()
                        ) ?: Visitor(
                            name = name.trim(),
                            company = company.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            source = source,
                            interestLevel = interestLevel,
                            status = status,
                            notes = notes.trim()
                        )
                        onConfirm(visitor)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_visitor_btn")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
