package com.example.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val preview: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val topic: String = "general",
    val language: String = "rajasthani"
)

@Entity(tableName = "cultural_articles")
data class CulturalArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val rajasthaniTitle: String,
    val category: String, // "forts", "cuisine", "arts", "proverbs", "festivals"
    val summary: String,
    val details: String,
    val culturalSignificance: String,
    val tags: String = "",
    val isBookmarked: Boolean = false
)

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val appLanguage: String = "Rajasthani", // "Rajasthani", "English", "Hindi"
    val isDarkMode: Boolean = true,
    val fontSizeScale: Float = 1.0f, // 0.85f (Small), 1.0f (Medium), 1.15f (Large), 1.3f (Extra Large)
    val isHighContrast: Boolean = false,
    val greetingStyle: String = "खम्मा घणी (Khamma Ghani)",
    val notificationsEnabled: Boolean = true,
    val userName: String = "Guest User",
    val userEmail: String = "guest@rajasthaniai.com",
    val isAuthenticated: Boolean = false
)
