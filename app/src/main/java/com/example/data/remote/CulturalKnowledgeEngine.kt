package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CulturalKnowledgeEngine {

    private val systemPrompt = """
        You are 'Rajasthan GPT' (राजस्थान जीपीटी), an authentic, warm, and highly knowledgeable AI assistant dedicated to Rajasthan's culture, heritage, history, royal dynasties, architecture, music, dance, folk tales, cuisine, and languages (Marwari, Mewari, Dhundhari, Harauti, Shekhawati, Hindi, and English).
        
        Guidelines:
        1. Always greet respectfully with traditional greetings like 'खम्मा घणी सा!' (Khamma Ghani Sa) or 'राम राम सा!' (Ram Ram Sa).
        2. When asked in English, answer in fluent English while weaving in authentic Rajasthani terminology and explanations.
        3. When asked in Rajasthani / Marwari / Hindi, respond authentically in that language with rich cultural flavor.
        4. Help users with:
           - Rajasthani language translation (e.g. English/Hindi to Marwari) and vocabulary
           - Forts, palaces, stepwells (Baori), and historical figures (Maharana Pratap, Rao Jodha, Rani Padmini, Mirabai)
           - Traditional food (Dal Baati Churma, Ker Sangri, Gatte ki Sabzi, Ghevar, Pyaaz Kachori)
           - Folk dances & music (Ghoomar, Kalbelia, Bhavai, Manganiyars, Langa musicians)
           - Proverbs (Kahavata) and cultural etiquette (Padharo Mhare Des).
        5. Keep responses engaging, structured, accurate, and culturally proud.
    """.trimIndent()

    suspend fun generateResponse(
        prompt: String,
        chatHistory: List<Pair<String, String>> = emptyList(),
        preferredLanguage: String = "Rajasthani"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = GeminiNetworkClient.getApiKey()

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val contentsList = mutableListOf<GeminiContent>()

                // Add history
                chatHistory.takeLast(6).forEach { (role, text) ->
                    contentsList.add(
                        GeminiContent(
                            role = if (role == "user") "user" else "model",
                            parts = listOf(GeminiPart(text = text))
                        )
                    )
                }

                // Add current prompt
                val fullUserPrompt = if (preferredLanguage == "Rajasthani") {
                    "$prompt\n(Please reply with cultural Rajasthani flair/flavor)."
                } else {
                    prompt
                }
                contentsList.add(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = fullUserPrompt))
                    )
                )

                val request = GeminiRequest(
                    contents = contentsList,
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = systemPrompt))
                    ),
                    generationConfig = GeminiGenerationConfig(
                        temperature = 0.7f,
                        maxOutputTokens = 1200
                    )
                )

                val response = GeminiNetworkClient.service.generateContent(apiKey, request)
                val textResult = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!textResult.isNullOrBlank()) {
                    return@withContext textResult.trim()
                }
            } catch (e: Exception) {
                Log.e("CulturalEngine", "Gemini API error, falling back to local cultural engine", e)
            }
        }

        // Offline Cultural Knowledge Fallback Engine
        return@withContext generateOfflineKnowledgeResponse(prompt, preferredLanguage)
    }

    private fun generateOfflineKnowledgeResponse(prompt: String, lang: String): String {
        val lower = prompt.lowercase()
        val isRaj = lang.equals("Rajasthani", ignoreCase = true)

        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("ram ram") || lower.contains("khamma") || lower.contains("namaste") -> {
                "खम्मा घणी सा! राम राम सा!\n\nWelcome to Rajasthani AI! I am your cultural companion for the royal land of Rajasthan. How may I assist you today?\n\nआप मुझसे क्या पूछना चाहते हैं? (Forts, Royal Cuisine, Marwari Language, Folk Lore, or History?)"
            }
            lower.contains("translate") || lower.contains("how do you say") || lower.contains("marwari") || lower.contains("meaning") -> {
                """
                खम्मा घणी! Here are essential Marwari & Rajasthani translations:

                • "How are you?" ➔ "थे क्यां हो सा?" (The kyan ho sa?) / "थांरा कांई हाल चाल है?"
                • "I am fine." ➔ "मैं मजे में हूँ सा / सब बढ़िया है।"
                • "Welcome to my home" ➔ "पधारो म्हारे घरे सा!"
                • "What is your name?" ➔ "थांरो शुभ नाम कांई है सा?"
                • "Thank you very much" ➔ "थांरो घणो-घणो आभार / धन्यवाद सा।"
                • "Have some water" ➔ "पाणी-पाणी पियो सा!"
                • "Where are you going?" ➔ "कठै चाल्या सा?"
                """.trimIndent()
            }
            lower.contains("food") || lower.contains("dish") || lower.contains("dal baati") || lower.contains("cuisine") || lower.contains("eat") || lower.contains("recipe") -> {
                """
                🍛 Royal Rajasthani Culinary Delicacies (राजस्थानी खान-पान):

                1. Dal Baati Churma (दाल बाटी चूरमो): The signature feast of golden baked baatis soaked in desi ghee, spicy panchmel dal, and sweet aromatic churma.
                2. Ker Sangri (केर सांगरी): Desert berry and bean delicacy harvested from Khejri trees, cooked with whole red chilies and amchur.
                3. Gatte ki Sabzi (गट्टे री सब्जी): Gram-flour dumplings simmered in rich spiced yogurt gravy.
                4. Pyaaz Kachori & Mirchi Vada (जोधपुरी मिर्ची वड़ा): Crispy breakfast street foods from Jodhpur.
                5. Ghevar (घेवर): Honeycomb sweet drenched in saffron sugar syrup and rabdi, celebrated during Teej and Raksha Bandhan.
                """.trimIndent()
            }
            lower.contains("fort") || lower.contains("palace") || lower.contains("mehrangarh") || lower.contains("amber") || lower.contains("hawa mahal") || lower.contains("kumbhalgarh") -> {
                """
                🏰 Majestic Forts & Palaces of Rajasthan (राजस्थान रा गौरवमयी दुर्ग):

                • Mehrangarh Fort (Jodhpur): Rao Jodha's 1459 cliff-top fortress with Sheesh Mahal and cannon battlements overlooking the Blue City.
                • Amber Fort & Palace (Jaipur): UNESCO World Heritage marvel above Maota Lake with the dazzling Mirror Palace.
                • Kumbhalgarh Fort (Mewar): Birthplace of Maharana Pratap, home to the world's 2nd largest perimeter wall (36 km).
                • Chittorgarh Fort: Symbol of Rajput chivalry, Jauhar sacrifice, and Rani Padmini's palace.
                • Hawa Mahal (Jaipur): 953 honeycombed jharokha windows capturing the desert breezes for royal queens.
                • Jaisalmer Fort (Sonar Qila): Living golden sandstone citadel rising amidst the Thar Desert.
                """.trimIndent()
            }
            lower.contains("dance") || lower.contains("music") || lower.contains("ghoomar") || lower.contains("kalbelia") || lower.contains("song") -> {
                """
                🎭 Rajasthani Folk Performing Arts & Music:

                • Ghoomar Dance (घूमर): Royal pirouette dance performed in swirling traditional ghaghras, originally dedicated to Goddess Saraswati.
                • Kalbelia Dance (कालबेलिया): UNESCO Intangible Cultural Heritage snake-charmer dance with acrobatic leaps to the rhythm of the 'Poongi'.
                • Bhavai (भवाई): Performer balances 7 to 9 brass pitchers on their head while dancing on swords or broken glass rims.
                • Folk Musicians: Manganiyars and Langas playing traditional instruments like Kamaicha, Khartal, Sarangi, and Morchang singing 'Kesariya Balam Aao Ni Padharo Mhare Des'.
                """.trimIndent()
            }
            lower.contains("proverb") || lower.contains("kahawat") || lower.contains("saying") || lower.contains("quote") -> {
                """
                📜 Famous Rajasthani Kahavata (राजस्थानी कहावतें):

                1. "पधारो म्हारे देस" (Padharo Mhare Des) - Welcome to our blessed land.
                2. "घी ढुल्यो तो थाली में" (Ghee dhulyo to thaali mein) - If the clarified butter spilled, it remained safely in the platter (No true loss among kin).
                3. "बाताँ सूं पेट कोनी भरे" (Baatan soon pet koni bhare) - Sweet talk cannot fill a hungry stomach (Actions speak louder than words).
                4. "आप मरे बिना सरग कोनी मिले" (Aap mare bina sarag koni mile) - One cannot see heaven without one's own effort.
                """.trimIndent()
            }
            lower.contains("history") || lower.contains("king") || lower.contains("pratap") || lower.contains("rajput") || lower.contains("war") -> {
                """
                ⚔️ Legendary History of Rajasthan:

                • Maharana Pratap of Mewar: Epitome of unyielding freedom, defending Mewar with his gallant steed Chetak at the Battle of Haldighati (1576).
                • Rao Jodha & Rao Bika: Founders of the Marwar Rathore dynasties of Jodhpur and Bikaner.
                • Mirabai of Merta: Mystic devotee and poetess whose bhajans resonate through every Rajasthani household.
                • Prithviraj Chauhan: The Chauhan ruler of Ajmer and Delhi celebrated in Chand Bardai's epic 'Prithviraj Raso'.
                """.trimIndent()
            }
            else -> {
                """
                खम्मा घणी सा! 
                
                You asked: "$prompt"

                In Rajasthan, every stone tells a story of valor, every desert dune whispers music, and every village welcomes you with "Padharo Mhare Des". 

                I can help you explore:
                1. 🏰 Forts, Palaces & Stepwells of Mewar, Marwar & Dhundhar
                2. 🗣️ Marwari language phrases, grammar & instant translation
                3. 🍲 Royal cuisine recipes (Dal Baati, Ker Sangri, Ghevar)
                4. 🎭 Folk dances (Ghoomar, Kalbelia, Bhavai) and Desert music
                5. 📜 Rajasthani proverbs and historical folklore

                What would you like to delve into next, सा?
                """.trimIndent()
            }
        }
    }
}
