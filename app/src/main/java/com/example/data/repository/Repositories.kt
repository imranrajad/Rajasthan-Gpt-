package com.example.data.repository

import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.CulturalDao
import com.example.data.local.dao.PreferencesDao
import com.example.data.local.model.ChatMessageEntity
import com.example.data.local.model.ChatSessionEntity
import com.example.data.local.model.CulturalArticleEntity
import com.example.data.local.model.UserPreferencesEntity
import com.example.data.remote.CulturalKnowledgeEngine
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ChatRepository(
    private val chatDao: ChatDao,
    private val culturalEngine: CulturalKnowledgeEngine = CulturalKnowledgeEngine()
) {
    val sessions: Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    fun getMessages(sessionId: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForSession(sessionId)

    suspend fun createSession(initialTitle: String = "Rajasthani Chat"): String {
        val id = UUID.randomUUID().toString()
        val session = ChatSessionEntity(
            id = id,
            title = initialTitle,
            preview = "खम्मा घणी! Start a conversation...",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        chatDao.insertSession(session)

        // Insert initial warm greeting from Rajasthan GPT
        val welcomeGreeting = "खम्मा घणी सा! राम राम सा! 🙏\n\nI am your Rajasthan GPT companion. Ask me anything about Rajasthan's royal forts, history, folk culture, recipes, or Marwari language translation!"
        chatDao.insertMessage(
            ChatMessageEntity(
                sessionId = id,
                role = "model",
                content = welcomeGreeting,
                topic = "greeting"
            )
        )
        return id
    }

    suspend fun sendMessage(
        sessionId: String,
        userPrompt: String,
        history: List<Pair<String, String>>,
        language: String
    ): String {
        // Save user message
        val userMsg = ChatMessageEntity(
            sessionId = sessionId,
            role = "user",
            content = userPrompt,
            language = language
        )
        chatDao.insertMessage(userMsg)

        // Update session preview & title if needed
        chatDao.updateSessionPreview(
            sessionId = sessionId,
            preview = if (userPrompt.length > 50) userPrompt.take(47) + "..." else userPrompt,
            updatedAt = System.currentTimeMillis()
        )

        // Generate response via Gemini or Cultural Engine
        val responseText = culturalEngine.generateResponse(
            prompt = userPrompt,
            chatHistory = history,
            preferredLanguage = language
        )

        // Save AI model message
        val aiMsg = ChatMessageEntity(
            sessionId = sessionId,
            role = "model",
            content = responseText,
            language = language
        )
        chatDao.insertMessage(aiMsg)

        return responseText
    }

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteMessagesForSession(sessionId)
        chatDao.deleteSession(sessionId)
    }
}

class CulturalRepository(private val culturalDao: CulturalDao) {
    val allArticles: Flow<List<CulturalArticleEntity>> = culturalDao.getAllArticles()

    fun getArticlesByCategory(category: String): Flow<List<CulturalArticleEntity>> {
        return if (category == "all") {
            culturalDao.getAllArticles()
        } else {
            culturalDao.getArticlesByCategory(category)
        }
    }

    suspend fun toggleBookmark(article: CulturalArticleEntity) {
        culturalDao.updateArticle(article.copy(isBookmarked = !article.isBookmarked))
    }
}

class PreferencesRepository(private val preferencesDao: PreferencesDao) {
    val preferences: Flow<UserPreferencesEntity?> = preferencesDao.getPreferences()

    suspend fun getPreferencesSync(): UserPreferencesEntity {
        return preferencesDao.getPreferencesSync() ?: UserPreferencesEntity()
    }

    suspend fun savePreferences(prefs: UserPreferencesEntity) {
        preferencesDao.savePreferences(prefs)
    }
}
