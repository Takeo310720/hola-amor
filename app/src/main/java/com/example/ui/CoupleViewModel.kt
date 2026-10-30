package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.util.ImageStorageHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class CoupleTab {
    CHAT,
    RECADOS,
    FOTOS,
    NOSOTROS
}

enum class RecadoFilter {
    ALL,
    PENDING,
    LOVE_NOTES,
    COMPLETED
}

class CoupleViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val recadoDao = db.recadoDao()
    private val chatDao = db.chatDao()
    private val photoDao = db.photoDao()
    private val prefs = CouplePreferences(application)

    val settings: StateFlow<CoupleSettings> = prefs.settingsFlow

    val messages: StateFlow<List<ChatMessage>> = chatDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recados: StateFlow<List<RecadoItem>> = recadoDao.getAllRecados()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val photos: StateFlow<List<CouplePhoto>> = photoDao.getAllPhotos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow(CoupleTab.CHAT)
    val currentTab: StateFlow<CoupleTab> = _currentTab.asStateFlow()

    private val _recadoFilter = MutableStateFlow(RecadoFilter.ALL)
    val recadoFilter: StateFlow<RecadoFilter> = _recadoFilter.asStateFlow()

    private val _fullscreenImageUri = MutableStateFlow<String?>(null)
    val fullscreenImageUri: StateFlow<String?> = _fullscreenImageUri.asStateFlow()

    fun setTab(tab: CoupleTab) {
        _currentTab.value = tab
    }

    fun setRecadoFilter(filter: RecadoFilter) {
        _recadoFilter.value = filter
    }

    fun openFullscreenImage(uri: String?) {
        _fullscreenImageUri.value = uri
    }

    fun closeFullscreenImage() {
        _fullscreenImageUri.value = null
    }

    // --- Active User Switching ---
    fun switchActiveSender() {
        val current = settings.value.activeSender
        val myName = settings.value.myName
        val partnerName = settings.value.partnerName
        val next = if (current == myName) partnerName else myName
        prefs.setActiveSender(next)
    }

    fun setActiveSender(name: String) {
        prefs.setActiveSender(name)
    }

    fun updateNames(myName: String, partnerName: String) {
        prefs.updateNames(myName, partnerName)
    }

    fun updatePartnerStatus(status: String) {
        prefs.updatePartnerStatus(status)
    }

    fun updateAnniversary(epochMillis: Long) {
        prefs.updateAnniversary(epochMillis)
    }

    fun updateLoveQuote(quote: String) {
        prefs.updateLoveQuote(quote)
    }

    // --- Chat Operations ---
    fun sendTextMessage(text: String) {
        if (text.isBlank()) return
        val currentSender = settings.value.activeSender
        viewModelScope.launch {
            val message = ChatMessage(
                sender = currentSender,
                text = text.trim(),
                timestamp = System.currentTimeMillis(),
                status = "READ"
            )
            chatDao.insertMessage(message)
        }
    }

    fun sendPhotoMessage(uri: Uri, caption: String = "") {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val internalPath = ImageStorageHelper.saveImageToInternalStorage(context, uri) ?: uri.toString()
            val currentSender = settings.value.activeSender
            val message = ChatMessage(
                sender = currentSender,
                text = caption.trim(),
                imageUri = internalPath,
                timestamp = System.currentTimeMillis(),
                status = "READ"
            )
            chatDao.insertMessage(message)

            // Also add to Couple Photos album automatically!
            photoDao.insertPhoto(
                CouplePhoto(
                    imageUri = internalPath,
                    caption = if (caption.isNotBlank()) caption else "Compartida por $currentSender en el chat ❤️",
                    tag = "Chat",
                    addedBy = currentSender
                )
            )
        }
    }

    fun sendVoiceNote(durationSeconds: Int = 4) {
        val currentSender = settings.value.activeSender
        viewModelScope.launch {
            val message = ChatMessage(
                sender = currentSender,
                text = "Nota de voz cariñosa 🎙️❤️",
                voiceDurationSec = durationSeconds,
                timestamp = System.currentTimeMillis(),
                status = "READ"
            )
            chatDao.insertMessage(message)
        }
    }

    fun toggleMessageReaction(messageId: Long, reaction: String) {
        viewModelScope.launch {
            val msg = messages.value.find { it.id == messageId } ?: return@launch
            val newReaction = if (msg.reaction == reaction) null else reaction
            chatDao.updateReaction(messageId, newReaction)
        }
    }

    fun toggleMessagePin(messageId: Long) {
        viewModelScope.launch {
            chatDao.togglePin(messageId)
        }
    }

    fun deleteMessage(message: ChatMessage) {
        viewModelScope.launch {
            chatDao.deleteMessage(message)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatDao.clearChat()
        }
    }

    // --- Recados Operations ---
    fun addRecado(
        title: String,
        details: String,
        category: String,
        target: String,
        dueDate: String,
        isPinned: Boolean
    ) {
        if (title.isBlank()) return
        val currentSender = settings.value.activeSender
        viewModelScope.launch {
            val recado = RecadoItem(
                title = title.trim(),
                details = details.trim(),
                category = category,
                author = currentSender,
                target = target,
                dueDate = dueDate.trim(),
                isPinned = isPinned,
                isCompleted = false,
                timestamp = System.currentTimeMillis()
            )
            recadoDao.insertRecado(recado)
        }
    }

    fun toggleRecadoCompleted(id: Long, currentCompleted: Boolean) {
        viewModelScope.launch {
            recadoDao.setCompleted(id, !currentCompleted)
        }
    }

    fun toggleRecadoPin(id: Long) {
        viewModelScope.launch {
            recadoDao.togglePin(id)
        }
    }

    fun deleteRecado(recado: RecadoItem) {
        viewModelScope.launch {
            recadoDao.deleteRecado(recado)
        }
    }

    // --- Photo Gallery Operations ---
    fun addPhotoToGallery(uri: Uri, caption: String, tag: String) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val internalPath = ImageStorageHelper.saveImageToInternalStorage(context, uri) ?: uri.toString()
            val currentSender = settings.value.activeSender
            val photo = CouplePhoto(
                imageUri = internalPath,
                caption = caption.trim(),
                tag = tag,
                addedBy = currentSender,
                timestamp = System.currentTimeMillis()
            )
            photoDao.insertPhoto(photo)
        }
    }

    fun togglePhotoFavorite(photoId: Long) {
        viewModelScope.launch {
            photoDao.toggleFavorite(photoId)
        }
    }

    fun deletePhoto(photo: CouplePhoto) {
        viewModelScope.launch {
            photoDao.deletePhoto(photo)
        }
    }
}
