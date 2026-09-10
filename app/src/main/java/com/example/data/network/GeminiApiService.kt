package com.example.data.network

import com.example.BuildConfig
import com.example.data.db.RemedyEntity
import com.example.data.model.ConsultationAiResponse
import com.example.data.model.MatchDetail
import com.example.data.model.PreliminaryAnswer
import com.example.data.model.QaItem
import com.example.data.model.ResearchDiscoveryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiApiService {
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun getConsultationNextStep(
        complaint: String,
        preliminaryAnswers: List<PreliminaryAnswer>,
        qaHistory: List<QaItem>,
        remedies: List<RemedyEntity>
    ): ConsultationAiResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Fallback response if API key is not configured yet
            return@withContext ConsultationAiResponse(
                action = "no_match",
                message = "I was unable to process your request because the Gemini API key is not configured."
            )
        }

        val prompt = buildConsultationPrompt(complaint, preliminaryAnswers, qaHistory, remedies)

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
                put("responseMimeType", "application/json")
            })
        }

        try {
            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBodyString = response.body?.string() ?: ""

            if (!response.isSuccessful || responseBodyString.isEmpty()) {
                return@withContext ConsultationAiResponse(
                    action = "no_match",
                    message = "I was unable to find a suitable medicine in my database based on the information provided."
                )
            }

            val jsonResponse = JSONObject(responseBodyString)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            parseAiResponse(text)
        } catch (e: Exception) {
            e.printStackTrace()
            ConsultationAiResponse(
                action = "no_match",
                message = "I was unable to find a suitable medicine in my database based on the information provided."
            )
        }
    }

    suspend fun getFollowUpAnswer(
        complaint: String,
        recommendedRemedy: String,
        explanation: String,
        benefit: String,
        remedyProfile: String,
        userQuestion: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Based on my knowledge base, that medicine isn't available in my records."
        }

        val prompt = """
            You are a warm, natural, conversational homeopathic clinical assistant speaking to an elderly patient. A consultation was completed with the following details:

            Patient Complaint: $complaint
            Recommended Remedy: $recommendedRemedy
            Why: $explanation
            Benefit: $benefit

            Remedy Documentation:
            $remedyProfile

            The patient is now asking a follow-up question: "$userQuestion"

            RULES:
            - Answer based ONLY on the provided documentation. Do not invent information.
            - Speak in a warm, simple, natural, conversational tone — like a friendly nurse or doctor talking to a patient.
            - NEVER use robotic or overly formal phrases like "The provided documentation does not contain..." or "I cannot provide information on this topic based on the consultation notes."
            - If the patient asks about a medicine, remedy, or topic that is NOT in your documentation, respond naturally and plainly, for example: "Based on my knowledge base, that medicine isn't available in my records." or "I don't have that remedy in my records, so I can't advise on it."
            - Be concise and direct. Keep answers short and easy to understand.
        """.trimIndent()

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.4)
            })
        }

        try {
            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBodyString = response.body?.string() ?: ""

            if (!response.isSuccessful || responseBodyString.isEmpty()) {
                return@withContext "Sorry, I could not process your question at this time."
            }

            val jsonResponse = JSONObject(responseBodyString)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            parts?.optJSONObject(0)?.optString("text")
                ?: "Sorry, I could not process your question at this time."
        } catch (e: Exception) {
            e.printStackTrace()
            "Sorry, I could not process your question at this time."
        }
    }

    suspend fun performInternetResearchAndVerification(
        remedyQuery: String,
        existingProfile: String?
    ): List<ResearchDiscoveryResult> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext emptyList()
        }

        val prompt = """
            You are an expert homeopathic bibliographic researcher, source verifier, and anti-forgery auditor.
            Your task is to conduct research on the homeopathic remedy: "$remedyQuery".

            Current existing profile in database:
            ${existingProfile ?: "None"}

            REQUIREMENTS FOR RESEARCH & VERIFICATION:
            1. Discover reliable homeopathic literature, published Materia Medica monographs, historical repertories, or modern clinical studies.
            2. For each discovered reference document, perform a strict SOURCE AUTHENTICITY AND ANTI-FORGERY CHECK:
               - Check if the book, author, publisher, and edition actually exist in established historical catalogs (e.g. Library of Congress, HathiTrust, Open Library, PubMed, National Library of Medicine).
               - Verify if the publication date and claimed ISBN make sense.
               - Detect if the document appears to be fabricated, forged, unconfirmed, or a random unreliable web article.
               - If authentic and independently corroboratable, assign verification_status: "VERIFIED".
               - If unconfirmed or lacking verifiable publisher/author records, assign "UNVERIFIED".
               - If suspicious, fabricated, or metadata is inconsistent, assign "POTENTIALLY_FORGED".
            3. Classify evidence into distinct categories:
               - "Historical Homeopathic Literature"
               - "Materia Medica Description"
               - "Traditional Homeopathic Claim"
               - "Modern Scientific Research"
               - "Clinical Study"
               - "Systematic Review"
            4. Compare extracted symptoms, modalities, and indications against the current existing profile.
            5. Detect CONFLICTS (e.g. contradictory modalities or opposing clinical indications). If conflicting, set has_conflicts = true and explain conflict_details.
            6. NEVER invent authors, books, ISBNs, or citations. If uncertain, mark verification_status as UNVERIFIED.

            Return a JSON ARRAY of 1 to 3 discovered source results:
            [
              {
                "remedy_name": "$remedyQuery",
                "source_title": "Full title of book / publication",
                "source_author": "Author name",
                "source_publisher": "Publisher name",
                "source_year": 1901,
                "edition": "Edition info if available",
                "isbn": "ISBN if available",
                "source_url": "URL if available",
                "source_type": "Original Book" | "Digitized Book" | "Journal Paper" | "Library Catalog" | "Traditional Reference",
                "verification_status": "VERIFIED" | "UNVERIFIED" | "POTENTIALLY_FORGED",
                "evidence_classification": "Historical Homeopathic Literature" | "Modern Scientific Research" | "Clinical Study",
                "confidence_score": 0.85,
                "authenticity_audit_notes": "Detailed notes on how authenticity was checked.",
                "extracted_profile": "Summary of discovered profile text",
                "extracted_symptoms": "Discovered symptom list",
                "extracted_modalities": "Worse/better factors",
                "extracted_claims": "Specific claims made by the text",
                "has_conflicts": false,
                "conflict_details": null,
                "supporting_sources": ["Corroborating reference 1"],
                "contradicting_sources": []
              }
            ]
        """.trimIndent()

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
                put("responseMimeType", "application/json")
            })
        }

        try {
            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBodyString = response.body?.string() ?: ""

            if (!response.isSuccessful || responseBodyString.isEmpty()) {
                return@withContext emptyList()
            }

            val jsonResponse = JSONObject(responseBodyString)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: "[]"

            parseResearchResults(rawText, remedyQuery)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseResearchResults(rawText: String, defaultRemedy: String): List<ResearchDiscoveryResult> {
        val list = mutableListOf<ResearchDiscoveryResult>()
        try {
            val cleanJson = rawText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val array = JSONArray(cleanJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val remedyName = obj.optString("remedy_name", defaultRemedy)
                val title = obj.optString("source_title", "Discovered Monograph")
                val author = obj.optString("source_author", null)
                val publisher = obj.optString("source_publisher", null)
                val year = if (obj.has("source_year")) obj.optInt("source_year") else null
                val edition = obj.optString("edition", null)
                val isbn = obj.optString("isbn", null)
                val url = obj.optString("source_url", null)
                val type = obj.optString("source_type", "Digitized Book")
                val status = obj.optString("verification_status", "UNVERIFIED")
                val classification = obj.optString("evidence_classification", "Materia Medica Description")
                val confidence = obj.optDouble("confidence_score", 0.75).toFloat()
                val auditNotes = obj.optString("authenticity_audit_notes", "Bibliographic cross-check performed.")
                val profile = obj.optString("extracted_profile", "")
                val symptoms = obj.optString("extracted_symptoms", "")
                val modalities = obj.optString("extracted_modalities", null)
                val claims = obj.optString("extracted_claims", null)
                val hasConflicts = obj.optBoolean("has_conflicts", false)
                val conflictDetails = obj.optString("conflict_details", null)

                val supporting = mutableListOf<String>()
                val supArr = obj.optJSONArray("supporting_sources")
                if (supArr != null) {
                    for (j in 0 until supArr.length()) supporting.add(supArr.getString(j))
                }

                val contradicting = mutableListOf<String>()
                val conArr = obj.optJSONArray("contradicting_sources")
                if (conArr != null) {
                    for (j in 0 until conArr.length()) contradicting.add(conArr.getString(j))
                }

                list.add(
                    ResearchDiscoveryResult(
                        remedyName = remedyName,
                        sourceTitle = title,
                        sourceAuthor = author,
                        sourcePublisher = publisher,
                        sourceYear = year,
                        edition = edition,
                        isbn = isbn,
                        sourceUrl = url,
                        sourceType = type,
                        verificationStatus = status,
                        evidenceClassification = classification,
                        confidenceScore = confidence,
                        authenticityAuditNotes = auditNotes,
                        extractedProfile = profile,
                        extractedSymptoms = symptoms,
                        extractedModalities = modalities,
                        extractedClaims = claims,
                        hasConflicts = hasConflicts,
                        conflictDetails = conflictDetails,
                        supportingSources = supporting,
                        contradictingSources = contradicting
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun buildConsultationPrompt(
        complaint: String,
        preliminaryAnswers: List<PreliminaryAnswer>,
        qaHistory: List<QaItem>,
        remedies: List<RemedyEntity>
    ): String {
        val remedyDocs = if (remedies.isEmpty()) "No documentation available." else {
            remedies.joinToString("\n\n") { remedy ->
                """
                === REMEDY: ${remedy.name} ===
                Source Material: ${remedy.source_material ?: "N/A"}
                ${remedy.full_profile}
                Indications: ${remedy.indications ?: "N/A"}
                Reasoning: ${remedy.reasoning ?: "N/A"}
                Benefits: ${remedy.benefits ?: "N/A"}
                Source Reference: ${remedy.source_reference ?: "N/A"}
                """.trimIndent()
            }
        }

        val prelimText = preliminaryAnswers.joinToString("\n") {
            "${it.questionLabel}: ${it.answer}"
        }

        val historyText = if (qaHistory.isEmpty()) "None" else {
            qaHistory.mapIndexed { idx, item ->
                "Q${idx + 1}: ${item.question}\nA${idx + 1}: ${item.answer}"
            }.joinToString("\n")
        }

        val historyCountNotice = if (qaHistory.size >= 10) {
            "\nNOTE: You have already asked ${qaHistory.size} questions. If you cannot make a confident recommendation now, return 'no_match'."
        } else ""

        return """
        You are a professional homeopathic clinical assistant designed for elderly patients. Your role is to conduct a brief, focused consultation and recommend the most suitable homeopathic remedy based STRICTLY on the provided documentation.

        STRICT RULES:
        1. You may ONLY use information from the AUTHORIZED DOCUMENTATION provided below.
        2. NEVER use outside medical knowledge. NEVER search the internet. NEVER invent information.
        3. NEVER invent medicines, symptoms, benefits, dosages, contraindications, or treatment instructions.
        4. Ask ONE question at a time. Questions must be short, formal, and clinician-like.
        5. Remember all previous answers. Do not repeat questions.
        6. Dynamically decide what question comes next based on the user's complaint, preliminary information, and previous answers.
        7. Stop asking questions when you have enough information to identify the best matching remedy.
        8. If the information is insufficient, ask clarifying questions to verify symptoms (symptom re-verification).
        9. If after re-verification you still cannot find a clear match in the documentation, return action "no_match" with the message: "I was unable to find a suitable medicine in my database based on the information provided."
        10. Never pretend you physically examined the patient.
        11. Communication style: formal, professional, respectful, calm, concise, direct. No casual conversation, emojis, excessive greetings, or technical AI terminology.

        RED FLAG DETECTION:
        If the user mentions any of these symptoms at ANY point, set has_red_flag to true:
        - Chest pain or pressure
        - Difficulty breathing or severe shortness of breath
        - Loss of consciousness, fainting, or confusion
        - Severe or uncontrolled bleeding
        - Sudden severe headache
        - Sudden weakness, numbness, or paralysis
        - Difficulty speaking or slurred speech
        - Severe vomiting or diarrhea (risk of dehydration)
        - High fever with stiff neck
        - Suicidal thoughts or severe mental distress

        When has_red_flag is true, still provide the recommendation if there is a documentation match, but include a red_flag_message advising the user to seek immediate professional medical attention.

        MATCHING LOGIC:
        - Compare the user's symptoms against the remedy profiles in the documentation.
        - Do NOT simply match a disease name. Look at symptom characteristics, modalities (worse/better factors), and mental/emotional state.
        - Select the remedy with the STRONGEST documented match.
        - When providing a recommendation, include match_details: an array of objects, each with:
          - patient_symptom: the symptom the patient described
          - database_info: the corresponding information from the remedy documentation
          - match_reason: why these correspond
        - Set confidence_level to:
          - "strong": multiple key symptoms clearly match the documented profile
          - "partial": some symptoms match but not all key features are present
          - "insufficient": weak match, uncertain
        - Never present a weak match as a strong one. If confidence is "partial" or "insufficient", state this clearly.
        - The "why" field must summarize the overall match explanation.
        - The "benefit" field must come from the documented general characteristics or key indications.
        - The "source" field must reference the specific source mentioned in the documentation.
        - If no supporting information exists in the database, return "no_match". Do NOT fill gaps with AI-generated information.

        JSON RESPONSE FORMAT:
        {
          "action": "ask_question" | "provide_recommendation" | "no_match",
          "question": "question text if action is ask_question",
          "question_type": "yes_no" | "multiple_choice" | "text",
          "options": ["option1", "option2"] (for multiple_choice),
          "medicine_name": "remedy name if provide_recommendation",
          "why": "explanation text",
          "match_details": [{"patient_symptom": "", "database_info": "", "match_reason": ""}],
          "confidence_level": "strong" | "partial" | "insufficient",
          "benefit": "expected benefit",
          "source": "source reference",
          "has_red_flag": true | false,
          "red_flag_message": "emergency message if red flag detected",
          "message": "no match message if no_match"
        }

        ---

        AUTHORIZED DOCUMENTATION (Use ONLY this information for recommendations):
        $remedyDocs

        ---

        PRELIMINARY PATIENT INFORMATION:
        $prelimText

        CONSULTATION:
        Patient Complaint: "$complaint"

        Previous Questions and Answers (${qaHistory.size} asked so far):
        $historyText
        $historyCountNotice

        Respond with JSON matching the specified format.
        """.trimIndent()
    }

    private fun parseAiResponse(rawText: String): ConsultationAiResponse {
        return try {
            val cleanJson = rawText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val obj = JSONObject(cleanJson)
            val action = obj.optString("action", "no_match")
            val question = obj.optString("question", null)
            val questionType = obj.optString("question_type", "text")

            val optionsList = mutableListOf<String>()
            val optionsArray = obj.optJSONArray("options")
            if (optionsArray != null) {
                for (i in 0 until optionsArray.length()) {
                    optionsList.add(optionsArray.getString(i))
                }
            }

            val medicineName = obj.optString("medicine_name", null)
            val why = obj.optString("why", null)

            val matchDetailsList = mutableListOf<MatchDetail>()
            val matchArray = obj.optJSONArray("match_details")
            if (matchArray != null) {
                for (i in 0 until matchArray.length()) {
                    val matchObj = matchArray.getJSONObject(i)
                    matchDetailsList.add(
                        MatchDetail(
                            patient_symptom = matchObj.optString("patient_symptom", ""),
                            database_info = matchObj.optString("database_info", ""),
                            match_reason = matchObj.optString("match_reason", "")
                        )
                    )
                }
            }

            val confidenceLevel = obj.optString("confidence_level", null)
            val benefit = obj.optString("benefit", null)
            val source = obj.optString("source", null)
            val hasRedFlag = obj.optBoolean("has_red_flag", false)
            val redFlagMessage = obj.optString("red_flag_message", null)
            val message = obj.optString("message", null)

            ConsultationAiResponse(
                action = action,
                question = question,
                question_type = questionType,
                options = if (optionsList.isNotEmpty()) optionsList else null,
                medicine_name = medicineName,
                why = why,
                match_details = if (matchDetailsList.isNotEmpty()) matchDetailsList else null,
                confidence_level = confidenceLevel,
                benefit = benefit,
                source = source,
                has_red_flag = hasRedFlag,
                red_flag_message = redFlagMessage,
                message = message
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ConsultationAiResponse(
                action = "no_match",
                message = "I was unable to find a suitable medicine in my database based on the information provided."
            )
        }
    }
}
