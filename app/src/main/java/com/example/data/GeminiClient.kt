package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class GeminiPart(
    val text: String? = null
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GoogleSearchTool(
    @Json(name = "google_search") val googleSearch: Map<String, String> = emptyMap()
)

data class GeminiRequest(
    val contents: List<GeminiContent>,
    @Json(name = "system_instruction") val systemInstruction: GeminiContent? = null,
    val tools: List<GoogleSearchTool>? = null
)

data class WebSource(
    val uri: String? = null,
    val title: String? = null
)

data class GroundingChunk(
    val web: WebSource? = null
)

data class GroundingMetadata(
    val webSearchQueries: List<String>? = null,
    val groundingChunks: List<GroundingChunk>? = null
)

data class GeminiCandidate(
    val content: GeminiContent,
    val groundingMetadata: GroundingMetadata? = null
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

data class SearchSource(
    val title: String,
    val url: String
)

data class ChatResponse(
    val text: String,
    val searchQueries: List<String>? = null,
    val sources: List<SearchSource>? = null
)

interface GeminiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiService = retrofit.create(GeminiService::class.java)

    suspend fun getChatResponse(prompt: String, systemInstruction: String? = null): ChatResponse {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return ChatResponse("त्रुटी: Gemini API की सेट केलेली नाही. कृपया AI Studio Secrets मध्ये GEMINI_API_KEY सेट करा. (Error: API Key not set.)")
        }

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            systemInstruction = systemInstruction?.let {
                GeminiContent(parts = listOf(GeminiPart(text = it)))
            },
            tools = listOf(GoogleSearchTool())
        )

        val modelsToTry = listOf(
            "gemini-2.5-flash",
            "gemini-2.0-flash",
            "gemini-1.5-flash",
            "gemini-3.5-flash",
            "gemini-3.1-pro-preview",
            "gemini-flash-latest"
        )

        var lastException: Exception? = null

        for (model in modelsToTry) {
            try {
                val response = service.generateContent(model, apiKey, request)
                val candidate = response.candidates?.firstOrNull()
                val text = candidate?.content?.parts?.firstOrNull()?.text
                if (text != null) {
                    val metadata = candidate.groundingMetadata
                    val queries = metadata?.webSearchQueries
                    val sources = metadata?.groundingChunks?.mapNotNull { chunk ->
                        val uri = chunk.web?.uri
                        val title = chunk.web?.title
                        if (uri != null) {
                            SearchSource(title = title ?: uri, url = uri)
                        } else null
                    }?.distinctBy { it.url }

                    return ChatResponse(
                        text = text,
                        searchQueries = if (queries.isNullOrEmpty()) null else queries,
                        sources = if (sources.isNullOrEmpty()) null else sources
                    )
                }
            } catch (e: Exception) {
                lastException = e
                // Continue to the next model in the list
            }
        }

        return ChatResponse("त्रुटी: AI सहाय्यक सध्या व्यस्त आहे किंवा उपलब्ध नाही. (Error: ${lastException?.localizedMessage ?: "काहीतरी चूक झाली."})")
    }
}
