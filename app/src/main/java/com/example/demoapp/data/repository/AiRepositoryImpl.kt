package com.example.demoapp.data.repository

import android.util.Log
import com.example.demoapp.BuildConfig
import com.example.demoapp.domain.model.AiEnrichment
import com.example.demoapp.domain.model.TouristPointCategory
import com.example.demoapp.domain.repository.AiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepositoryImpl @Inject constructor() : AiRepository {

    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta"
    // gemini-1.5-flash y text-embedding-004 quedaron deprecados (404 en v1beta).
    private val genModel = "gemini-2.5-flash-lite"
    private val embedModel = "gemini-embedding-001"
    private val tag = "AiRepository"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val mediaType = "application/json".toMediaType()
    private val client = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun enrichPoint(
        title: String,
        description: String,
        category: TouristPointCategory,
        imageLabels: List<String>
    ): Result<AiEnrichment> = withContext(Dispatchers.IO) {
        runCatching {
            ensureApiKey()
            val prompt = buildEnrichmentPrompt(title, description, category, imageLabels)
            val rawJson = generateJson(prompt)
            val cleanJson = rawJson.replace("json", "").replace("", "").trim()
            val parsed = json.decodeFromString(EnrichmentJson.serializer(), cleanJson)

            val embedSource = listOf(
                title,
                parsed.improvedDescription.ifBlank { description },
                category.name,
                parsed.tags.joinToString(" ")
            ).filter { it.isNotBlank() }.joinToString(". ")

            val embedding = embed(embedSource)

            AiEnrichment(
                tags = parsed.tags.map { it.trim() }.filter { it.isNotBlank() },
                improvedDescription = parsed.improvedDescription.ifBlank { null },
                embedding = embedding
            )
        }
    }

    override suspend fun embedQuery(query: String): Result<List<Double>> = withContext(Dispatchers.IO) {
        runCatching {
            ensureApiKey()
            embed(query)
        }
    }

    // ── Internals ────────────────────────────────────────────────────────────

    private fun ensureApiKey() {
        if (apiKey.isBlank()) {
            error("GEMINI_API_KEY no configurada. Agregala en local.properties.")
        }
    }

    private fun buildEnrichmentPrompt(
        title: String,
        description: String,
        category: TouristPointCategory,
        imageLabels: List<String>
    ): String {
        val labelsJoined = if (imageLabels.isEmpty()) "(sin etiquetas de imagen)"
        else imageLabels.joinToString(", ")
        val categoryHuman = when (category) {
            TouristPointCategory.NATURE        -> "Naturaleza (paisajes, montanas, rios, parques)"
            TouristPointCategory.GASTRONOMY    -> "Gastronomia (comida tipica, restaurantes, sabores)"
            TouristPointCategory.CULTURE       -> "Cultura (historia, museos, tradiciones)"
            TouristPointCategory.ENTERTAINMENT -> "Arte urbano y entretenimiento"
            else                               -> "Otro"
        }
        return """
            Eres un copywriter de una app de turismo. Devuelve SOLO JSON valido (sin markdown ni texto extra) con esta forma exacta:
            {"tags":["..."],"improvedDescription":"..."}

            Reglas:
            - 3 a 5 tags cortos en espanol (1 a 2 palabras, minusculas, sin emojis)
            - improvedDescription: maximo 200 caracteres, en espanol, en tono INVITANTE que motive a visitar el lugar.
            - Usa adjetivos atractivos segun lo que muestre la imagen: "hermoso paisaje", "rica comida tipica", "vista impresionante", "ambiente acogedor", etc.
            - Apoyate en las etiquetas de la imagen y la categoria para describir el lugar; no inventes nombres de personas ni datos especificos.
            - Si la descripcion del usuario esta vacia, redactala desde cero a partir de las etiquetas de imagen y la categoria.

            Datos del lugar:
            - titulo: $title
            - categoria: $categoryHuman
            - descripcion del usuario: ${description.ifBlank { "(vacia)" }}
            - etiquetas detectadas en la imagen (ML Kit): $labelsJoined
        """.trimIndent()
    }

    private fun generateJson(prompt: String): String {
        val req = GenerateRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.4
            )
        )
        val body = json.encodeToString(GenerateRequest.serializer(), req).toRequestBody(mediaType)
        val url = "$baseUrl/models/$genModel:generateContent?key=$apiKey"
        Log.d(tag, "generateContent -> $genModel")

        repeat(2) { attempt ->
            client.newCall(Request.Builder().url(url).post(body).build()).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                if (resp.code == 429 && attempt == 0) {
                    val waitMs = extractRetryAfterMs(raw) ?: 3000L
                    Log.w(tag, "generateContent 429, reintentando en ${waitMs}ms")
                    Thread.sleep(waitMs)
                    return@use
                }
                if (!resp.isSuccessful) {
                    Log.e(tag, "generateContent ${resp.code}: $raw")
                    error("Gemini generate ${resp.code}: $raw")
                }
                val parsed = json.decodeFromString(GenerateResponse.serializer(), raw)
                val text = parsed.candidates.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text
                    ?: run {
                        Log.e(tag, "generateContent vacio: $raw")
                        error("Respuesta vacia de Gemini")
                    }
                Log.d(tag, "generateContent OK (${text.length} chars)")
                return text
            }
        }
        error("Gemini generate 429: limite de solicitudes")
    }

    private fun extractRetryAfterMs(raw: String): Long? {
        // Gemini devuelve un campo "retryDelay":"30s" dentro del error. Lo extraemos a mano.
        val match = Regex("\"retryDelay\"\\s*:\\s*\"(\\d+(?:\\.\\d+)?)s\"").find(raw)
        return match?.groupValues?.get(1)?.toDoubleOrNull()?.let { (it * 1000).toLong() }
    }

    private fun embed(text: String): List<Double> {
        val req = EmbedRequest(
            model = "models/$embedModel",
            content = GeminiContent(parts = listOf(GeminiPart(text)))
        )
        val body = json.encodeToString(EmbedRequest.serializer(), req).toRequestBody(mediaType)
        val url = "$baseUrl/models/$embedModel:embedContent?key=$apiKey"
        Log.d(tag, "embedContent -> $embedModel")
        client.newCall(Request.Builder().url(url).post(body).build()).execute().use { resp ->
            val raw = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                Log.e(tag, "embedContent ${resp.code}: $raw")
                error("Gemini embed ${resp.code}: $raw")
            }
            val parsed = json.decodeFromString(EmbedResponse.serializer(), raw)
            Log.d(tag, "embedContent OK (dim=${parsed.embedding.values.size})")
            return parsed.embedding.values
        }
    }

    // ── DTOs ─────────────────────────────────────────────────────────────────

    @Serializable
    private data class GeminiPart(val text: String)

    @Serializable
    private data class GeminiContent(
        val parts: List<GeminiPart>,
        val role: String? = null
    )

    @Serializable
    private data class GenerationConfig(
        val responseMimeType: String? = null,
        val temperature: Double? = null
    )

    @Serializable
    private data class GenerateRequest(
        val contents: List<GeminiContent>,
        val generationConfig: GenerationConfig? = null
    )

    @Serializable
    private data class GenerateCandidate(val content: GeminiContent? = null)

    @Serializable
    private data class GenerateResponse(val candidates: List<GenerateCandidate> = emptyList())

    @Serializable
    private data class EmbedRequest(
        val model: String,
        val content: GeminiContent
    )

    @Serializable
    private data class EmbedValues(val values: List<Double> = emptyList())

    @Serializable
    private data class EmbedResponse(val embedding: EmbedValues = EmbedValues())

    @Serializable
    private data class EnrichmentJson(
        val tags: List<String> = emptyList(),
        val improvedDescription: String = ""
    )
}