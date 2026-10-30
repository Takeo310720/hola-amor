package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.ChatMessage
import com.example.data.CoupleSettings
import com.example.ui.CoupleViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: CoupleViewModel,
    settings: CoupleSettings,
    messages: List<ChatMessage>,
    onOpenPhoto: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var textInput by remember { mutableStateOf("") }
    var showEmojiBar by remember { mutableStateOf(false) }
    var showCallDialog by remember { mutableStateOf(false) }
    var showClearChatDialog by remember { mutableStateOf(false) }
    var selectedMessageForOptions by remember { mutableStateOf<ChatMessage?>(null) }

    // Photo Picker contract
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.sendPhotoMessage(uri, textInput)
            textInput = ""
        }
    }

    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val pinnedMessage = messages.findLast { it.isPinned }
    val partnerDisplayName = if (settings.activeSender == settings.myName) settings.partnerName else settings.myName

    fun triggerHaptic() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(50)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = partnerDisplayName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 18.sp
                            )
                            // Online indicator dot
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                                    .align(Alignment.BottomEnd)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Partner name and status
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = partnerDisplayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = settings.partnerStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        }

                        // Perspective switcher chip (allows typing as César or Partner)
                        FilterChip(
                            selected = true,
                            onClick = {
                                triggerHaptic()
                                viewModel.switchActiveSender()
                            },
                            label = {
                                Text(
                                    text = "Yo: ${settings.activeSender}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Cambiar remitente",
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            modifier = Modifier.testTag("switch_sender_chip")
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showCallDialog = true },
                        modifier = Modifier.testTag("call_partner_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Llamar a pareja",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { showClearChatDialog = true },
                        modifier = Modifier.testTag("clear_chat_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones de chat"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Quick emoji bar
                AnimatedVisibility(
                    visible = showEmojiBar,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    val loveEmojis = listOf("❤️", "🥰", "😘", "💌", "🌹", "🥐", "✨", "🔥", "🥺", "💍")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        loveEmojis.forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 22.sp,
                                modifier = Modifier
                                    .clickable {
                                        triggerHaptic()
                                        textInput += emoji
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                // Input bar (WhatsApp style)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showEmojiBar = !showEmojiBar },
                        modifier = Modifier.testTag("emoji_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (showEmojiBar) Icons.Default.Keyboard else Icons.Outlined.Mood,
                            contentDescription = "Emojis de amor",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Text Field
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Escribe a tu amor...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    // Photo attachment button (Photo Picker)
                    IconButton(
                        onClick = {
                            triggerHaptic()
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.testTag("attach_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Adjuntar foto",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Voice note / quick audio button
                    IconButton(
                        onClick = {
                            triggerHaptic()
                            viewModel.sendVoiceNote(4)
                        },
                        modifier = Modifier.testTag("voice_note_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Nota de voz de amor",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Send Button
                    FloatingActionButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                triggerHaptic()
                                viewModel.sendTextMessage(textInput)
                                textInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .testTag("send_message_button"),
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar mensaje",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    if (MaterialTheme.colorScheme.surface == WarmSurfaceDark) ChatBackgroundDark else ChatBackgroundLight
                )
        ) {
            // Pinned message banner if any
            if (pinnedMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedMessageForOptions = pinnedMessage
                        }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Mensaje fijado",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fijado: ${pinnedMessage.text.ifEmpty { "Foto de amor" }}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Message list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Romantic header badge
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "🔒 Espacio privado solo de ${settings.myName} y ${settings.partnerName} ❤️",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                items(messages, key = { it.id }) { message ->
                    val isMe = message.sender == settings.activeSender
                    MessageBubble(
                        message = message,
                        isMe = isMe,
                        onOpenPhoto = onOpenPhoto,
                        onLongClick = {
                            triggerHaptic()
                            selectedMessageForOptions = message
                        }
                    )
                }
            }
        }
    }

    // Message options dialog (reactions, pin, delete)
    if (selectedMessageForOptions != null) {
        val msg = selectedMessageForOptions!!
        AlertDialog(
            onDismissRequest = { selectedMessageForOptions = null },
            title = { Text("Reaccionar o gestionar mensaje ❤️") },
            text = {
                Column {
                    Text(
                        text = "Elige una reacción con amor:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("❤️", "🥰", "😘", "🔥", "💌", "✨").forEach { reaction ->
                            Text(
                                text = reaction,
                                fontSize = 28.sp,
                                modifier = Modifier
                                    .clickable {
                                        triggerHaptic()
                                        viewModel.toggleMessageReaction(msg.id, reaction)
                                        selectedMessageForOptions = null
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    TextButton(
                        onClick = {
                            viewModel.toggleMessagePin(msg.id)
                            selectedMessageForOptions = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (msg.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (msg.isPinned) "Desfijar del chat" else "Fijar mensaje arriba")
                    }

                    TextButton(
                        onClick = {
                            viewModel.deleteMessage(msg)
                            selectedMessageForOptions = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminar mensaje")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMessageForOptions = null }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Sweet Playful Call Dialog
    if (showCallDialog) {
        AlertDialog(
            onDismissRequest = { showCallDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Llamada a ${partnerDisplayName}")
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Conectando corazones... ❤️📞")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "\"No importa la distancia, tú siempre estás en mi mente.\" 🥰",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        triggerHaptic()
                        viewModel.sendTextMessage("¡Te mandé una llamada de amor! 📞❤️")
                        showCallDialog = false
                    }
                ) {
                    Text("Mandar zumbido de amor")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCallDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Clear chat dialog
    if (showClearChatDialog) {
        AlertDialog(
            onDismissRequest = { showClearChatDialog = false },
            title = { Text("¿Vaciar historial del chat?") },
            text = { Text("Se borrarán los mensajes actuales de la conversación de los dos.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearChat()
                        showClearChatDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Vaciar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearChatDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isMe: Boolean,
    onOpenPhoto: (String) -> Unit,
    onLongClick: () -> Unit
) {
    val bubbleColor = if (isMe) {
        if (MaterialTheme.colorScheme.surface == WarmSurfaceDark) ChatBubbleSentDark else ChatBubbleSentLight
    } else {
        if (MaterialTheme.colorScheme.surface == WarmSurfaceDark) ChatBubbleReceivedDark else ChatBubbleReceivedLight
    }

    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormatter.format(Date(message.timestamp)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Surface(
                color = bubbleColor,
                shape = bubbleShape,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .clickable(onClick = onLongClick)
                    .testTag("chat_bubble_${message.id}")
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Sender name if received
                    if (!isMe) {
                        Text(
                            text = message.sender,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    // Attached Photo
                    if (message.imageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 140.dp, max = 240.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onOpenPhoto(message.imageUri) }
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(message.imageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto compartida en chat",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        if (message.text.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    // Voice Note representation
                    if (message.voiceDurationSec > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Reproducir nota de voz",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    .padding(4.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Waveform bars
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(12, 20, 16, 24, 10, 18, 22, 14, 26, 12, 18).forEach { height ->
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(height.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                                RoundedCornerShape(2.dp)
                                            )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "0:0${message.voiceDurationSec}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Message text
                    if (message.text.isNotBlank()) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isMe && MaterialTheme.colorScheme.surface != WarmSurfaceDark) Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Timestamp and checkmarks row
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Fijado",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(12.dp)
                                    .padding(end = 4.dp)
                            )
                        }
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        if (isMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Leído por mi amor",
                                tint = ChatDoubleCheckBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Floating reaction badge if any
            if (message.reaction != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .offset(y = (-6).dp)
                        .padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = message.reaction,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
