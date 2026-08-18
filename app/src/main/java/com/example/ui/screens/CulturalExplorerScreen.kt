package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Fort
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.model.CulturalArticleEntity
import com.example.ui.components.CulturalBadge
import com.example.ui.components.CulturalHeader
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSurface
import com.example.ui.theme.BrandSurfaceElevated
import com.example.ui.viewmodel.MainViewModel

data class CategoryTab(val id: String, val label: String, val icon: ImageVector)

@Composable
fun CulturalExplorerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val articles by viewModel.culturalArticles.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val preferences by viewModel.userPreferences.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedArticleForDetails by remember { mutableStateOf<CulturalArticleEntity?>(null) }

    val categories = listOf(
        CategoryTab("all", "All (सबै)", Icons.Default.AutoAwesome),
        CategoryTab("forts", "🏰 Forts & Palaces", Icons.Default.Fort),
        CategoryTab("cuisine", "🍲 Royal Cuisine", Icons.Default.Restaurant),
        CategoryTab("arts", "🎭 Folk Arts", Icons.Default.MusicNote),
        CategoryTab("proverbs", "📜 Proverbs (कहावतें)", Icons.Default.MenuBook),
        CategoryTab("festivals", "🎪 Festivals", Icons.Default.Celebration)
    )

    val filteredArticles = articles.filter { article ->
        (selectedCategory == "all" || article.category.equals(selectedCategory, ignoreCase = true)) &&
                (searchQuery.isBlank() ||
                        article.title.contains(searchQuery, ignoreCase = true) ||
                        article.rajasthaniTitle.contains(searchQuery, ignoreCase = true) ||
                        article.summary.contains(searchQuery, ignoreCase = true))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        item {
            CulturalHeader(
                title = "Rajasthani Heritage Guide",
                subtitle = "Explore royal history, art, cuisine & language wisdom (/api/get-info)"
            )
        }

        // Search Bar
        item {
            PaddingValues(horizontal = 16.dp, vertical = 8.dp).let {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search forts, food, proverbs, arts...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = BrandAccent
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BrandSurfaceElevated,
                        unfocusedContainerColor = BrandSurfaceElevated,
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true
                )
            }
        }

        // Category Filter Chips Carousel
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setCategory(cat.id) },
                        label = {
                            Text(
                                text = cat.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = BrandSurface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        // Article Cards
        items(filteredArticles) { article ->
            val isExpanded = selectedArticleForDetails?.id == article.id

            Card(
                onClick = {
                    selectedArticleForDetails = if (isExpanded) null else article
                },
                colors = CardDefaults.cardColors(
                    containerColor = BrandSurface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .border(
                        1.dp,
                        if (isExpanded) BrandAccent.copy(alpha = 0.5f) else Color(0x223B82F6),
                        RoundedCornerShape(16.dp)
                    )
                    .testTag("cultural_article_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CulturalBadge(
                            text = article.category.uppercase(),
                            color = when (article.category) {
                                "forts" -> BrandGold
                                "cuisine" -> Color(0xFFF97316)
                                "arts" -> Color(0xFFEC4899)
                                "proverbs" -> BrandPrimary
                                else -> BrandAccent
                            }
                        )
                        IconButton(
                            onClick = { viewModel.toggleArticleBookmark(article) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (article.isBookmarked) BrandGold else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = article.rajasthaniTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = BrandAccent
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = article.summary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = (14 * preferences.fontSizeScale).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = BrandSurfaceElevated,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Detailed Insights:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BrandGold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = article.details,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = (13 * preferences.fontSizeScale).sp,
                                        lineHeight = (19 * preferences.fontSizeScale).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Cultural Significance:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BrandAccent
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = article.culturalSignificance,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.askAiAboutArticle(article) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ask Rajasthani AI about this",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
