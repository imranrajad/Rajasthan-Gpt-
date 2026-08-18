package com.example.ui.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.database.AppDatabase
import com.example.data.local.model.ChatMessageEntity
import com.example.data.local.model.ChatSessionEntity
import com.example.data.local.model.CulturalArticleEntity
import com.example.data.local.model.UserPreferencesEntity
import com.example.data.repository.ChatRepository
import com.example.data.repository.CulturalRepository
import com.example.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

sealed class NavigationScreen(val route: String, val title: String) {
    object Home : NavigationScreen("home", "Home")
    object Chat : NavigationScreen("chat", "AI Chat")
    object Explore : NavigationScreen("explore", "Heritage")
    object Settings : NavigationScreen("settings", "Settings")
}

class MainViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val db = AppDatabase.getInstance(application)
    private val chatRepo = ChatRepository(db.chatDao())
    private val culturalRepo = CulturalRepository(db.culturalDao())
    private val prefRepo = PreferencesRepository(db.preferencesDao())

    // TTS
    private var tts: TextToSpeech? = null
    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    init {
        tts = TextToSpeech(application, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("hi", "IN")
            _isTtsReady.value = true
        }
    }

    fun speakText(text: String) {
        if (_isTtsReady.value) {
            val cleanText = text.replace("*", "").replace("#", "")
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "tts_msg_id")
        }
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }

    // Navigation State
    private val _currentScreen = MutableStateFlow<NavigationScreen>(NavigationScreen.Home)
    val currentScreen: StateFlow<NavigationScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: NavigationScreen) {
        _currentScreen.value = screen
    }

    // Preferences
    val userPreferences: StateFlow<UserPreferencesEntity> = prefRepo.preferences
        .map { it ?: UserPreferencesEntity() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferencesEntity()
        )

    fun updateTheme(isDark: Boolean) {
        viewModelScope.launch {
            val curr = userPreferences.value
            prefRepo.savePreferences(curr.copy(isDarkMode = isDark))
        }
    }

    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            val curr = userPreferences.value
            prefRepo.savePreferences(curr.copy(appLanguage = lang))
        }
    }

    fun updateFontSize(scale: Float) {
        viewModelScope.launch {
            val curr = userPreferences.value
            prefRepo.savePreferences(curr.copy(fontSizeScale = scale))
        }
    }

    fun updateHighContrast(enabled: Boolean) {
        viewModelScope.launch {
            val curr = userPreferences.value
            prefRepo.savePreferences(curr.copy(isHighContrast = enabled))
        }
    }

    fun updateNotifications(enabled: Boolean) {
        viewModelScope.launch {
            val curr = userPreferences.value
            prefRepo.savePreferences(curr.copy(notificationsEnabled = enabled))
        }
    }

    fun signInUser(name: String, email: String) {
        viewModelScope.launch {
            val curr = userPreferences.value
            prefRepo.savePreferences(
                curr.copy(
                    userName = name,
                    userEmail = email,
                    isAuthenticated = true
                )
            )
        }
    }

    fun signOutUser() {
        viewModelScope.launch {
            val curr = userPreferences.value
            prefRepo.savePreferences(
                curr.copy(
                    userName = "Guest User",
                    userEmail = "guest@rajasthaniai.com",
                    isAuthenticated = false
                )
            )
        }
    }

    // Chat State
    val chatSessions: StateFlow<List<ChatSessionEntity>> = chatRepo.sessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

    private val _activeMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val activeMessages: StateFlow<List<ChatMessageEntity>> = _activeMessages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    fun selectSession(sessionId: String) {
        _activeSessionId.value = sessionId
        viewModelScope.launch {
            chatRepo.getMessages(sessionId).collect { messages ->
                _activeMessages.value = messages
            }
        }
        _currentScreen.value = NavigationScreen.Chat
    }

    fun startNewChat(initialPrompt: String? = null, topic: String = "Rajasthani Chat") {
        viewModelScope.launch {
            val newId = chatRepo.createSession(topic)
            _activeSessionId.value = newId
            _currentScreen.value = NavigationScreen.Chat

            // Start observing messages for the new session
            launch {
                chatRepo.getMessages(newId).collect { msgs ->
                    _activeMessages.value = msgs
                }
            }

            if (!initialPrompt.isNullOrBlank()) {
                sendMessage(initialPrompt)
            }
        }
    }

    fun sendMessage(prompt: String) {
        val sessionId = _activeSessionId.value ?: return
        if (prompt.isBlank() || _isGenerating.value) return

        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val currentHistory = _activeMessages.value.map { it.role to it.content }
                val preferredLang = userPreferences.value.appLanguage
                chatRepo.sendMessage(sessionId, prompt.trim(), currentHistory, preferredLang)
            } catch (e: Exception) {
                // handled in repo
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            chatRepo.deleteSession(sessionId)
            if (_activeSessionId.value == sessionId) {
                _activeSessionId.value = null
                _activeMessages.value = emptyList()
            }
        }
    }

    // Cultural Explorer State
    val culturalArticles: StateFlow<List<CulturalArticleEntity>> = culturalRepo.allArticles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleArticleBookmark(article: CulturalArticleEntity) {
        viewModelScope.launch {
            culturalRepo.toggleBookmark(article)
        }
    }

    fun askAiAboutArticle(article: CulturalArticleEntity) {
        val question = "Tell me in detail about ${article.title} (${article.rajasthaniTitle}), its royal history, and cultural significance in Rajasthan."
        startNewChat(initialPrompt = question, topic = article.title)
    }
}
