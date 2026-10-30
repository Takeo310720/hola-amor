package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CoupleSettings(
    val myName: String = "César",
    val partnerName: String = "Mi Amor",
    val activeSender: String = "César", // Who is currently typing/active
    val partnerStatus: String = "Pensando en ti ❤️",
    val anniversaryEpochMillis: Long = System.currentTimeMillis() - (180L * 24 * 60 * 60 * 1000L), // ~6 months ago
    val loveQuote: String = "Juntos es nuestro lugar favorito ✨",
    val isAppLocked: Boolean = false,
    val lockPin: String = "1234"
)

class CouplePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tuyyo_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<CoupleSettings> = _settingsFlow.asStateFlow()

    fun loadSettings(): CoupleSettings {
        return CoupleSettings(
            myName = prefs.getString("my_name", "César") ?: "César",
            partnerName = prefs.getString("partner_name", "Mi Amor") ?: "Mi Amor",
            activeSender = prefs.getString("active_sender", "César") ?: "César",
            partnerStatus = prefs.getString("partner_status", "Pensando en ti ❤️") ?: "Pensando en ti ❤️",
            anniversaryEpochMillis = prefs.getLong(
                "anniversary_epoch",
                System.currentTimeMillis() - (180L * 24 * 60 * 60 * 1000L)
            ),
            loveQuote = prefs.getString("love_quote", "Juntos es nuestro lugar favorito ✨")
                ?: "Juntos es nuestro lugar favorito ✨",
            isAppLocked = prefs.getBoolean("is_locked", false),
            lockPin = prefs.getString("lock_pin", "1234") ?: "1234"
        )
    }

    fun updateNames(myName: String, partnerName: String) {
        prefs.edit()
            .putString("my_name", myName.trim())
            .putString("partner_name", partnerName.trim())
            .apply()
        _settingsFlow.value = loadSettings()
    }

    fun setActiveSender(senderName: String) {
        prefs.edit().putString("active_sender", senderName).apply()
        _settingsFlow.value = loadSettings()
    }

    fun updatePartnerStatus(status: String) {
        prefs.edit().putString("partner_status", status).apply()
        _settingsFlow.value = loadSettings()
    }

    fun updateAnniversary(epochMillis: Long) {
        prefs.edit().putLong("anniversary_epoch", epochMillis).apply()
        _settingsFlow.value = loadSettings()
    }

    fun updateLoveQuote(quote: String) {
        prefs.edit().putString("love_quote", quote).apply()
        _settingsFlow.value = loadSettings()
    }

    fun toggleAppLock(locked: Boolean, pin: String = "1234") {
        prefs.edit().putBoolean("is_locked", locked).putString("lock_pin", pin).apply()
        _settingsFlow.value = loadSettings()
    }
}
