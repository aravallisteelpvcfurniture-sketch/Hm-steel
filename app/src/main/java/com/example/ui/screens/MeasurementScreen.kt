package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Measurement
import com.example.data.model.MeasurementUnit
import com.example.ui.MainViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val measurements by viewModel.measurements.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var locationName by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var lengthInput by remember { mutableStateOf("20.0") }
    var widthInput by remember { mutableStateOf("15.0") }
    var heightInput by remember { mutableStateOf("10.0") }
    var selectedUnit by remember { mutableStateOf(settings.defaultUnit) }
    var notesInput by remember { mutableStateOf("") }

    val length = lengthInput.toDoubleOrNull() ?: 1.0
    val width = widthInput.toDoubleOrNull() ?: 1.0
    val height = heightInput.toDoubleOrNull() ?: 0.0

    // Temporary measurement for live preview
    val livePreview = remember(locationName, clientName, length, width, height, selectedUnit) {
        Measurement(
            locationName = if (locationName.isBlank()) "Live Room Preview" else locationName,
            clientName = clientName,
            length = length,
            width = width,
            height = height,
            unit = selectedUnit
        )
    }

    var showSuccessSnackbar by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("measurement_screen"),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Blueprint Visualizer Canvas
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📐 Live Blueprint Preview",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0284C7))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", livePreview.floorArea)} ${selectedUnit.areaLabel}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Interactive Blueprint Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            val canvasW = size.width
                            val canvasH = size.height

                            // Draw subtle grid lines
                            val gridSpacing = 24.dp.toPx()
                            var x = 0f
                            while (x < canvasW) {
                                drawLine(
                                    color = Color(0x2238BDF8),
                                    start = Offset(x, 0f),
                                    end = Offset(x, canvasH),
                                    strokeWidth = 1f
                                )
                                x += gridSpacing
                            }
                            var y = 0f
                            while (y < canvasH) {
                                drawLine(
                                    color = Color(0x2238BDF8),
                                    start = Offset(0f, y),
                                    end = Offset(canvasW, y),
                                    strokeWidth = 1f
                                )
                                y += gridSpacing
                            }

                            // Calculate aspect ratio for length and width
                            val maxDimension = maxOf(length, width, 1.0)
                            val availableW = canvasW - 40.dp.toPx()
                            val availableH = canvasH - 40.dp.toPx()

                            val scale = minOf(availableW / maxDimension, availableH / maxDimension).toFloat()
                            val rectW = (length * scale).toFloat().coerceIn(40f, availableW)
                            val rectH = (width * scale).toFloat().coerceIn(40f, availableH)

                            val rectLeft = (canvasW - rectW) / 2f
                            val rectTop = (canvasH - rectH) / 2f

                            // Draw Area Rectangle
                            drawRect(
                                color = Color(0x330EA5E9),
                                topLeft = Offset(rectLeft, rectTop),
                                size = Size(rectW, rectH)
                            )
                            drawRect(
                                color = Color(0xFF38BDF8),
                                topLeft = Offset(rectLeft, rectTop),
                                size = Size(rectW, rectH),
                                style = Stroke(
                                    width = 3.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 8f), 0f)
                                )
                            )

                            // Dimension arrows/lines
                            // Top Length
                            drawLine(
                                color = Color(0xFFFBBF24),
                                start = Offset(rectLeft, rectTop - 12f),
                                end = Offset(rectLeft + rectW, rectTop - 12f),
                                strokeWidth = 2.dp.toPx()
                            )
                            // Left Width
                            drawLine(
                                color = Color(0xFFFBBF24),
                                start = Offset(rectLeft - 12f, rectTop),
                                end = Offset(rectLeft - 12f, rectTop + rectH),
                                strokeWidth = 2.dp.toPx()
                            )
                        }

                        // Center blueprint text overlay
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", length)} ${selectedUnit.code} × ${String.format(Locale.US, "%.1f", width)} ${selectedUnit.code}",
                                color = Color(0xFFFBBF24),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Perimeter: ${String.format(Locale.US, "%.1f", livePreview.perimeter)} ${selectedUnit.code}",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                            if (height > 0) {
                                Text(
                                    text = "Wall Area: ${String.format(Locale.US, "%.1f", livePreview.wallSurfaceArea)} ${selectedUnit.areaLabel}",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Material Estimation Breakdown Cards
        item {
            Text(
                text = "Estimated Material Requirements",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EstimationChipCard(
                    modifier = Modifier.weight(1f),
                    title = "Flooring Tiles",
                    amount = "${livePreview.flooringBoxesEstimate} Boxes",
                    detail = "+10% wastage included",
                    icon = Icons.Default.GridOn,
                    color = Color(0xFF0D9488)
                )
                EstimationChipCard(
                    modifier = Modifier.weight(1f),
                    title = "Paint Needed",
                    amount = "${livePreview.paintGallonsEstimate} Gal",
                    detail = "2 Coats coverage",
                    icon = Icons.Default.FormatPaint,
                    color = Color(0xFF4338CA)
                )
            }
        }

        // Measurement Inputs Form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "New Site Survey / Area",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedTextField(
                        value = locationName,
                        onValueChange = { locationName = it },
                        label = { Text("Area / Location Name *") },
                        placeholder = { Text("e.g. Expo Booth 4B, Reception Lobby") },
                        modifier = Modifier.fillMaxWidth().testTag("measure_location_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Client / Project Name") },
                        placeholder = { Text("e.g. Vance Global") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Unit selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Measurement Unit:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MeasurementUnit.values().forEach { unit ->
                                FilterChip(
                                    selected = selectedUnit == unit,
                                    onClick = { selectedUnit = unit },
                                    label = { Text(unit.code) }
                                )
                            }
                        }
                    }

                    // Dimension fields
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = lengthInput,
                            onValueChange = { lengthInput = it },
                            label = { Text("Length (${selectedUnit.code})") },
                            modifier = Modifier.weight(1f).testTag("measure_length_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = widthInput,
                            onValueChange = { widthInput = it },
                            label = { Text("Width (${selectedUnit.code})") },
                            modifier = Modifier.weight(1f).testTag("measure_width_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = heightInput,
                            onValueChange = { heightInput = it },
                            label = { Text("Height (opt)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Site Notes & Installation Details") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Button(
                        onClick = {
                            if (locationName.isNotBlank() && length > 0 && width > 0) {
                                val newMeasurement = Measurement(
                                    locationName = locationName.trim(),
                                    clientName = clientName.trim(),
                                    length = length,
                                    width = width,
                                    height = height,
                                    unit = selectedUnit,
                                    notes = notesInput.trim()
                                )
                                viewModel.addMeasurement(newMeasurement)
                                locationName = ""
                                clientName = ""
                                notesInput = ""
                                showSuccessSnackbar = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("save_measurement_btn"),
                        enabled = locationName.isNotBlank() && length > 0 && width > 0,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Measurement Record")
                    }

                    if (showSuccessSnackbar) {
                        Text(
                            text = "✓ Measurement saved successfully to records!",
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Saved Measurements History
        item {
            Text(
                text = "Saved Measurements (${measurements.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (measurements.isEmpty()) {
            item {
                Text(
                    text = "No saved measurements yet. Enter dimensions above to create one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(measurements, key = { it.id }) { item ->
                SavedMeasurementCard(
                    measurement = item,
                    onDelete = { viewModel.deleteMeasurement(item.id) },
                    onCreateEstimate = { viewModel.startEstimateFromMeasurement(item) }
                )
            }
        }
    }
}

@Composable
fun EstimationChipCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: String,
    detail: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.10f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = amount, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(text = detail, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp))
        }
    }
}

@Composable
fun SavedMeasurementCard(
    measurement: Measurement,
    onDelete: () -> Unit,
    onCreateEstimate: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = measurement.locationName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (measurement.clientName.isNotBlank()) {
                        Text(
                            text = "Client: ${measurement.clientName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dimension stats pill row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Dimensions", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${String.format(Locale.US, "%.1f", measurement.length)} × ${String.format(Locale.US, "%.1f", measurement.width)} ${measurement.unit.code}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Floor Area", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${String.format(Locale.US, "%.1f", measurement.floorArea)} ${measurement.unit.areaLabel}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Perimeter", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${String.format(Locale.US, "%.1f", measurement.perimeter)} ${measurement.unit.code}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (measurement.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = measurement.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onCreateEstimate,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate Estimate from this Measurement")
            }
        }
    }
}
