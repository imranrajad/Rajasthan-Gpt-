package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.model.ChatMessageEntity
import com.example.data.local.model.ChatSessionEntity
import com.example.data.local.model.CulturalArticleEntity
import com.example.data.local.model.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): ChatSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)

    @Query("UPDATE chat_sessions SET preview = :preview, updatedAt = :updatedAt WHERE id = :sessionId")
    suspend fun updateSessionPreview(sessionId: String, preview: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getMessageCount(): Int
}

@Dao
interface CulturalDao {
    @Query("SELECT * FROM cultural_articles")
    fun getAllArticles(): Flow<List<CulturalArticleEntity>>

    @Query("SELECT * FROM cultural_articles WHERE category = :category")
    fun getArticlesByCategory(category: String): Flow<List<CulturalArticleEntity>>

    @Query("SELECT * FROM cultural_articles WHERE isBookmarked = 1")
    fun getBookmarkedArticles(): Flow<List<CulturalArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<CulturalArticleEntity>)

    @Update
    suspend fun updateArticle(article: CulturalArticleEntity)

    @Query("SELECT COUNT(*) FROM cultural_articles")
    suspend fun getArticleCount(): Int
}

@Dao
interface PreferencesDao {
    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun getPreferences(): Flow<UserPreferencesEntity?>

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    suspend fun getPreferencesSync(): UserPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreferences(preferences: UserPreferencesEntity)
}
