package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CoupleTab
import com.example.ui.CoupleViewModel
import com.example.ui.components.FullscreenImageViewer
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CoupleProfileScreen
import com.example.ui.screens.PhotosScreen
import com.example.ui.screens.RecadosScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: CoupleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CoupleApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CoupleApp(viewModel: CoupleViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val recados by viewModel.recados.collectAsStateWithLifecycle()
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val recadoFilter by viewModel.recadoFilter.collectAsStateWithLifecycle()
    val fullscreenImageUri by viewModel.fullscreenImageUri.collectAsStateWithLifecycle()

    // Handle Back Button: if fullscreen image is open, close it; if not in Chat, return to Chat tab
    BackHandler(enabled = fullscreenImageUri != null || currentTab != CoupleTab.CHAT) {
        if (fullscreenImageUri != null) {
            viewModel.closeFullscreenImage()
        } else if (currentTab != CoupleTab.CHAT) {
            viewModel.setTab(CoupleTab.CHAT)
        }
    }

    val pendingRecadosCount = recados.count { !it.isCompleted }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.testTag("main_bottom_nav"),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    // Chat Tab
                    NavigationBarItem(
                        selected = currentTab == CoupleTab.CHAT,
                        onClick = { viewModel.setTab(CoupleTab.CHAT) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == CoupleTab.CHAT) Icons.Default.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                                contentDescription = stringResource(R.string.tab_chat)
                            )
                        },
                        label = { Text(stringResource(R.string.tab_chat)) },
                        modifier = Modifier.testTag("nav_item_chat")
                    )

                    // Recados Tab (with badge of pending items)
                    NavigationBarItem(
                        selected = currentTab == CoupleTab.RECADOS,
                        onClick = { viewModel.setTab(CoupleTab.RECADOS) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (pendingRecadosCount > 0) {
                                        Badge {
                                            Text("$pendingRecadosCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentTab == CoupleTab.RECADOS) Icons.Default.EditNote else Icons.Outlined.EditNote,
                                    contentDescription = stringResource(R.string.tab_recados)
                                )
                            }
                        },
                        label = { Text(stringResource(R.string.tab_recados)) },
                        modifier = Modifier.testTag("nav_item_recados")
                    )

                    // Photos Tab
                    NavigationBarItem(
                        selected = currentTab == CoupleTab.FOTOS,
                        onClick = { viewModel.setTab(CoupleTab.FOTOS) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == CoupleTab.FOTOS) Icons.Default.PhotoLibrary else Icons.Outlined.PhotoLibrary,
                                contentDescription = stringResource(R.string.tab_fotos)
                            )
                        },
                        label = { Text(stringResource(R.string.tab_fotos)) },
                        modifier = Modifier.testTag("nav_item_fotos")
                    )

                    // Profile / Nosotros Tab
                    NavigationBarItem(
                        selected = currentTab == CoupleTab.NOSOTROS,
                        onClick = { viewModel.setTab(CoupleTab.NOSOTROS) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == CoupleTab.NOSOTROS) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = stringResource(R.string.tab_nosotros)
                            )
                        },
                        label = { Text(stringResource(R.string.tab_nosotros)) },
                        modifier = Modifier.testTag("nav_item_nosotros")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    CoupleTab.CHAT -> ChatScreen(
                        viewModel = viewModel,
                        settings = settings,
                        messages = messages,
                        onOpenPhoto = { uri -> viewModel.openFullscreenImage(uri) }
                    )
                    CoupleTab.RECADOS -> RecadosScreen(
                        viewModel = viewModel,
                        settings = settings,
                        recados = recados,
                        activeFilter = recadoFilter
                    )
                    CoupleTab.FOTOS -> PhotosScreen(
                        viewModel = viewModel,
                        settings = settings,
                        photos = photos,
                        onOpenPhoto = { uri -> viewModel.openFullscreenImage(uri) }
                    )
                    CoupleTab.NOSOTROS -> CoupleProfileScreen(
                        viewModel = viewModel,
                        settings = settings,
                        messagesCount = messages.size,
                        recadosCompletedCount = recados.count { it.isCompleted },
                        photosCount = photos.size
                    )
                }
            }
        }

        // Fullscreen Photo Viewer overlay
        if (fullscreenImageUri != null) {
            FullscreenImageViewer(
                imageUri = fullscreenImageUri,
                caption = "",
                onDismiss = { viewModel.closeFullscreenImage() }
            )
        }
    }
}
