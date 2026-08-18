package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.CulturalDao
import com.example.data.local.dao.PreferencesDao
import com.example.data.local.model.ChatMessageEntity
import com.example.data.local.model.ChatSessionEntity
import com.example.data.local.model.CulturalArticleEntity
import com.example.data.local.model.UserPreferencesEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        CulturalArticleEntity::class,
        UserPreferencesEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun culturalDao(): CulturalDao
    abstract fun preferencesDao(): PreferencesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rajasthani_app_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).seedInitialData()
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun seedInitialData() {
        // Seed default preferences
        preferencesDao().savePreferences(
            UserPreferencesEntity(
                id = 1,
                appLanguage = "Rajasthani",
                isDarkMode = true,
                fontSizeScale = 1.0f,
                isHighContrast = false,
                greetingStyle = "खम्मा घणी (Khamma Ghani)",
                notificationsEnabled = true,
                userName = "Guest User",
                userEmail = "guest@rajasthaniai.com",
                isAuthenticated = false
            )
        )

        // Seed comprehensive Rajasthani cultural knowledge
        val articles = listOf(
            CulturalArticleEntity(
                id = "mehrangarh_fort",
                title = "Mehrangarh Fort (Jodhpur)",
                rajasthaniTitle = "मेहरानगढ़ दुर्ग (जोधपुर)",
                category = "forts",
                summary = "One of the largest and most majestic forts in India, towering 410 feet above the Blue City of Jodhpur.",
                details = "Built in 1459 by Rao Jodha, Mehrangarh Fort stands on a perpendicular cliff. Its walls are up to 36 meters high and 21 meters wide. It houses Sheesh Mahal (Mirror Palace), Phool Mahal (Flower Palace), and Moti Mahal (Pearl Palace), preserving palanquins, turbans, miniature paintings, and royal cannons like Kilkila.",
                culturalSignificance = "Symbolizes Rathore valor and architectural grandeur in Marwar.",
                tags = "Fort, Jodhpur, Architecture, Rathore, History"
            ),
            CulturalArticleEntity(
                id = "amber_fort",
                title = "Amber Fort & Palace (Jaipur)",
                rajasthaniTitle = "आमेर को किलो (जयपुर)",
                category = "forts",
                summary = "UNESCO World Heritage site known for its artistic Hindu elements, Maota Lake, and Sheesh Mahal.",
                details = "Constructed by Raja Man Singh I in 1592, Amer Fort features pale yellow and pink sandstone. The Sheesh Mahal is inlaid with thousands of concave mirrors that illuminate the room with a single flame. The fort connects through underground passages to the defensive Jaigarh Fort.",
                culturalSignificance = "Epitome of Rajput-Mughal syncretic architectural finesse in Dhundhar.",
                tags = "Jaipur, Sheesh Mahal, UNESCO, Rajput, Heritage"
            ),
            CulturalArticleEntity(
                id = "hawa_mahal",
                title = "Hawa Mahal (Palace of Winds)",
                rajasthaniTitle = "हवा महल (जयपुर)",
                category = "forts",
                summary = "A five-story crown-shaped palace with 953 intricately carved jharokhas (small windows).",
                details = "Built in 1799 by Maharaja Sawai Pratap Singh and designed by Lal Chand Ustad, the palace resembles the crown of Lord Krishna. The lattice windows allowed royal ladies to observe daily life and street festivals unnoticed, while channeling the Venturi cooling breeze through the palace.",
                culturalSignificance = "Iconic landmark embodying Rajasthani royal modesty and thermal cooling ingenuity.",
                tags = "Jaipur, Architecture, Jharokha, Breeze, Landmark"
            ),
            CulturalArticleEntity(
                id = "kumbhalgarh_fort",
                title = "Kumbhalgarh Fort (Rajsamand)",
                rajasthaniTitle = "कुम्भलगढ़ दुर्ग (मेवाड़)",
                category = "forts",
                summary = "The birthplace of Maharana Pratap, boasting the second-longest continuous wall in the world (36 km).",
                details = "Built during the 15th century by Rana Kumbha, this Mewar fortress was impregnable and served as a refuge in times of war. The perimeter wall stretches 36 kilometers across the Aravalli hills, wide enough for eight horses to walk abreast. It holds over 360 temples inside.",
                culturalSignificance = "Pride of Mewar defense, UNESCO World Heritage fortress.",
                tags = "Mewar, Great Wall, Maharana Pratap, Aravalli"
            ),
            CulturalArticleEntity(
                id = "dal_baati_churma",
                title = "Dal Baati Churma",
                rajasthaniTitle = "दाल बाटी चूरमो",
                category = "cuisine",
                summary = "The quintessential tripartite royal and rustic Rajasthani feast.",
                details = "Comprises three harmonious components: 'Baati' (hard, unleavened wheat flour balls baked over cow-dung cake embers and drowned in pure desi ghee), 'Panchmel Dal' (a spicy slow-simmered blend of 5 lentils tempered with cloves, hing, and dried red chilies), and 'Churma' (sweet crushed baatis sweetened with jaggery or boora sugar, green cardamom, and dry fruits).",
                culturalSignificance = "Originated during wartime in Mewar for long shelf life and high caloric sustenance; now the royal signature of Rajasthani hospitality.",
                tags = "Food, Signature Dish, Ghee, Feast, Royal"
            ),
            CulturalArticleEntity(
                id = "ker_sangri",
                title = "Ker Sangri (Desert Delicacy)",
                rajasthaniTitle = "केर सांगरी रो साग",
                category = "cuisine",
                summary = "A traditional dry subzi made from dried wild caper berries (Ker) and desert bean pods (Sangri).",
                details = "Harvested from the Khejri tree (Prosopis cineraria) and Ker shrubs in the Thar desert. The berries and beans are dried for preservation, then soaked overnight and cooked in mustard oil with amchur (dried mango powder), whole red chilies, and aromatic spices. Requires minimal water to prepare.",
                culturalSignificance = "Showcases the ingenuity of Marwari culinary survival in arid desert climates.",
                tags = "Thar Desert, Khejri, Traditional, Vegan, Wild"
            ),
            CulturalArticleEntity(
                id = "ghoomar_dance",
                title = "Ghoomar Folk Dance",
                rajasthaniTitle = "घूमर नृत्य",
                category = "arts",
                summary = "The traditional graceful royal dance of Rajasthan, performed with twirling ghaghras.",
                details = "Originally developed by the Bhil tribe to worship Goddess Saraswati, later embraced by royal Rajput clans. Women wear vibrant swirling ghaghras (skirts) and translucent odhnis, executing pirouettes (ghoomna) while clapping in synchronized rhythm to dholak, nagada, and harmonium.",
                culturalSignificance = "Recognized globally as one of the world's most elegant heritage folk dances.",
                tags = "Dance, Royal, Ghoomar, Folk Music, Tradition"
            ),
            CulturalArticleEntity(
                id = "kalbelia_dance",
                title = "Kalbelia (Snake Charmer Dance)",
                rajasthaniTitle = "कालबेलिया नृत्य",
                category = "arts",
                summary = "UNESCO Intangible Cultural Heritage dance of the nomadic Kalbelia snake-charmer community.",
                details = "Dancers wear flowing black skirts embroidered with silver ribbons and colorful glass beads, replicating the undulating, serpentine movements of a cobra. Accompanied by the 'Poongi' (wooden pipe), Khanjari, and Dholak, the tempo rises to exhilarating acrobatic feats.",
                culturalSignificance = "Celebrates harmonious living with desert reptiles and nomadic folklore.",
                tags = "UNESCO, Snake Charmer, Poongi, Nomad, Music"
            ),
            CulturalArticleEntity(
                id = "rajasthani_proverbs",
                title = "Rajasthani Kahavata (Proverbs)",
                rajasthaniTitle = "राजस्थानी कहावतें व मुहावरा",
                category = "proverbs",
                summary = "Centuries of desert wisdom and witty cultural idioms preserved in Marwari speech.",
                details = "1. 'पधारो म्हारे देस' (Padharo Mhare Des) - Welcome to our land with folded hands and open heart.\n2. 'खम्मा घणी' (Khamma Ghani) - May you be blessed with abundant forgiveness and auspicious health.\n3. 'घी ढुल्यो तो थाली में' (Ghee dhulyo to thaali mein) - Even if the clarified butter spilled, it remained safely in the royal platter (A loss within the family is no loss at all).\n4. 'बाताँ सूं पेट कोनी भरे' (Baatan soon pet koni bhare) - Mere words cannot satisfy a hungry belly (Action matters more than words).\n5. 'हाथी रा दाँत दिखावा रा और, खावा रा और' - Elephant has different tusks for show and teeth for eating (Appearance vs reality).",
                culturalSignificance = "Encapsulates ethical resilience, humor, and communal bonding.",
                tags = "Wisdom, Idioms, Marwari, Kahawat, Language"
            ),
            CulturalArticleEntity(
                id = "pushkar_fair",
                title = "Pushkar Camel Fair (Mela)",
                rajasthaniTitle = "पुष्कर ऊँट मेलो",
                category = "festivals",
                summary = "The world's largest livestock and cultural fair held on the banks of sacred Pushkar Lake.",
                details = "Celebrated on Kartik Purnima (October-November). Over 50,000 camels, horses, and cattle are decorated with pom-poms, silver anklets, and bead embroidery. Features moustache competitions, bridal dressing contests, turban tying, and folk music evenings.",
                culturalSignificance = "Bridges spiritual pilgrimage to the only Lord Brahma temple with vibrant rural commerce.",
                tags = "Pushkar, Camel Fair, Kartik Purnima, Brahma, Mela"
            )
        )

        culturalDao().insertArticles(articles)
    }
}
