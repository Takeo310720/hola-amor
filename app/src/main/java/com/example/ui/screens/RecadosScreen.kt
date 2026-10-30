package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CoupleSettings
import com.example.data.RecadoItem
import com.example.ui.CoupleViewModel
import com.example.ui.RecadoFilter
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecadosScreen(
    viewModel: CoupleViewModel,
    settings: CoupleSettings,
    recados: List<RecadoItem>,
    activeFilter: RecadoFilter
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedRecadoForEdit by remember { mutableStateOf<RecadoItem?>(null) }

    val filteredRecados = remember(recados, activeFilter) {
        when (activeFilter) {
            RecadoFilter.ALL -> recados
            RecadoFilter.PENDING -> recados.filter { !it.isCompleted }
            RecadoFilter.LOVE_NOTES -> recados.filter { it.category == "Amor" }
            RecadoFilter.COMPLETED -> recados.filter { it.isCompleted }
        }
    }

    val pendingCount = recados.count { !it.isCompleted }
    val loveNotesCount = recados.count { it.category == "Amor" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Nuestros Recados 💌",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$pendingCount pendientes • $loveNotesCount cartitas de amor",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("add_recado_top_button"),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nuevo")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.testTag("add_recado_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Crear nuevo recado")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Filter chips row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = activeFilter == RecadoFilter.ALL,
                        onClick = { viewModel.setRecadoFilter(RecadoFilter.ALL) },
                        label = { Text("Todos (${recados.size})") },
                        modifier = Modifier.testTag("filter_all_recados")
                    )
                }
                item {
                    FilterChip(
                        selected = activeFilter == RecadoFilter.PENDING,
                        onClick = { viewModel.setRecadoFilter(RecadoFilter.PENDING) },
                        label = { Text("📌 Pendientes ($pendingCount)") },
                        modifier = Modifier.testTag("filter_pending_recados")
                    )
                }
                item {
                    FilterChip(
                        selected = activeFilter == RecadoFilter.LOVE_NOTES,
                        onClick = { viewModel.setRecadoFilter(RecadoFilter.LOVE_NOTES) },
                        label = { Text("💌 Cartitas ($loveNotesCount)") },
                        modifier = Modifier.testTag("filter_love_notes")
                    )
                }
                item {
                    FilterChip(
                        selected = activeFilter == RecadoFilter.COMPLETED,
                        onClick = { viewModel.setRecadoFilter(RecadoFilter.COMPLETED) },
                        label = { Text("✓ Hechos (${recados.count { it.isCompleted }})") },
                        modifier = Modifier.testTag("filter_completed_recados")
                    )
                }
            }

            // Recados List
            if (filteredRecados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay recados en esta lista",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Toca el botón '+' para dejarle un recadito, una lista de compras o una nota de amor a tu pareja.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRecados, key = { it.id }) { recado ->
                        RecadoCard(
                            recado = recado,
                            onToggleComplete = { viewModel.toggleRecadoCompleted(recado.id, recado.isCompleted) },
                            onTogglePin = { viewModel.toggleRecadoPin(recado.id) },
                            onDelete = { viewModel.deleteRecado(recado) }
                        )
                    }
                }
            }
        }
    }

    // Add Recado Dialog
    if (showAddDialog) {
        AddRecadoDialog(
            activeSender = settings.activeSender,
            partnerName = if (settings.activeSender == settings.myName) settings.partnerName else settings.myName,
            onDismiss = { showAddDialog = false },
            onAdd = { title, details, category, target, dueDate, isPinned ->
                viewModel.addRecado(title, details, category, target, dueDate, isPinned)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun RecadoCard(
    recado: RecadoItem,
    onToggleComplete: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    // Dynamic soft colors based on category
    val (categoryColor, categoryIcon) = when (recado.category) {
        "Amor" -> Pair(Color(0xFFFFCCD5), "💌")
        "Compras" -> Pair(Color(0xFFD8F3DC), "🛒")
        "Cita" -> Pair(Color(0xFFFFE5D9), "🍿")
        "Urgente" -> Pair(Color(0xFFFFD6A5), "⚠️")
        else -> Pair(Color(0xFFE2ECE9), "📌")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recado_card_${recado.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (recado.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (recado.isPinned) 3.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Category badge, Author & Pin
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = categoryColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "$categoryIcon ${recado.category}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = recado.target,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (recado.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Fijar recado",
                            tint = if (recado.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Eliminar recado",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Checkbox and Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = recado.isCompleted,
                    onCheckedChange = { onToggleComplete() },
                    modifier = Modifier.testTag("recado_checkbox_${recado.id}")
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recado.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (recado.isCompleted) TextDecoration.LineThrough else null,
                        color = if (recado.isCompleted)
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.onSurface
                    )

                    if (recado.details.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = recado.details,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Due date and Author footer
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (recado.dueDate.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = recado.dueDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Text(
                            text = "De: ${recado.author}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecadoDialog(
    activeSender: String,
    partnerName: String,
    onDismiss: () -> Unit,
    onAdd: (title: String, details: String, category: String, target: String, dueDate: String, isPinned: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Pendiente") }
    var target by remember { mutableStateOf("Para los dos") }
    var dueDate by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }

    val categories = listOf("Pendiente", "Amor", "Compras", "Cita", "Urgente")
    val targets = listOf("Para los dos", "Para $partnerName", "Para mí")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Nuevo recado para nosotros ❤️", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título del recado *") },
                    placeholder = { Text("Ej. Comprar pan dulce o Recordar cita") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recado_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Detalle o mensajito de amor") },
                    placeholder = { Text("Opcional: nota cariñosa...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Text(
                    text = "Categoría:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Text(
                    text = "Destinatario:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    targets.forEach { tgt ->
                        FilterChip(
                            selected = target == tgt,
                            onClick = { target = tgt },
                            label = { Text(tgt, fontSize = 12.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("¿Cuándo? (Opcional)") },
                    placeholder = { Text("Ej. Hoy 8:00 PM o Sábado") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Fijar al inicio del tablero")
                    Switch(checked = isPinned, onCheckedChange = { isPinned = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, details, category, target, dueDate, isPinned)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_recado_button")
            ) {
                Text("Guardar recado")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
