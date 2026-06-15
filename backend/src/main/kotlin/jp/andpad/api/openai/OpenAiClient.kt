package jp.andpad.api.openai

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

/** OpenAI Chat Completions（建設 PM 向け AI 相談・分析）。 */
@Component
class OpenAiClient(properties: OpenAiProperties) {

    private val log = LoggerFactory.getLogger(OpenAiClient::class.java)
    private val apiKey: String = properties.apiKey?.trim().orEmpty()
    private val model: String = if (StringUtils.hasText(properties.model)) properties.model!!.trim() else "gpt-4o-mini"
    private val http: RestClient = RestClient.builder()
        .baseUrl("https://api.openai.com")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()

    fun isEnabled(): Boolean = StringUtils.hasText(apiKey)

    fun chat(systemPrompt: String, history: List<OpenAiChatMessage>?, userMessage: String): String {
        if (!isEnabled()) {
            throw IllegalStateException("OPENAI_API_KEY is not configured")
        }
        val messages = mutableListOf<OpenAiChatMessage>()
        messages.add(OpenAiChatMessage("system", systemPrompt))
        if (history != null) {
            for (msg in history) {
                if (msg.content.isNotBlank()) {
                    messages.add(msg)
                }
            }
        }
        messages.add(OpenAiChatMessage("user", userMessage))

        return try {
            val response = http.post()
                .uri("/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $apiKey")
                .body(OpenAiChatRequest(model, messages))
                .retrieve()
                .body(OpenAiChatResponse::class.java)
                ?: throw IllegalStateException("openai: empty response")

            if (response.error != null && StringUtils.hasText(response.error.message)) {
                throw IllegalStateException("openai: ${response.error.message}")
            }
            if (response.choices.isNullOrEmpty()) {
                throw IllegalStateException("openai: no choices")
            }
            val answer = response.choices.first().message
                ?: throw IllegalStateException("openai: empty message")
            if (!StringUtils.hasText(answer.content)) {
                throw IllegalStateException("openai: empty message")
            }
            answer.content.trim()
        } catch (ex: RestClientResponseException) {
            log.warn("OpenAI HTTP {}: {}", ex.statusCode.value(), ex.responseBodyAsString)
            throw IllegalStateException(
                "openai http ${ex.statusCode.value()}: ${truncate(ex.responseBodyAsString, 120)}",
            )
        } catch (ex: IllegalStateException) {
            throw ex
        } catch (ex: Exception) {
            log.warn("OpenAI chat failed: {}", ex.message)
            throw IllegalStateException(truncate(ex.message, 120), ex)
        }
    }

    companion object {
        val CONSTRUCTION_CONSULT_SYSTEM: String = """
            You are an AI assistant for construction project management professionals in Japan (ANDPAD-style platform).
            Help with site safety, schedule coordination, subcontractor management, quality inspection, document workflows, BIM/digital delivery, and general construction PM best practices.
            Be practical and concise. When unsure, say what information is needed and suggest checking site rules or consulting a qualified supervisor.
            Do not provide legally binding engineering sign-off. Respond in Japanese unless the user writes in another language.""".trimIndent()

        val CONSTRUCTION_ANALYTICS_SYSTEM: String = """
            You are a construction project management analyst (AI Board). Given JSON analytics KPIs for a construction PM platform, respond ONLY with valid JSON:
            {"summary":"...","strengths":["..."],"risks":["..."],"recommendations":["..."]}
            Write in Japanese. Focus on project delivery, schedule risk, module adoption, billing trends, safety/compliance awareness, and actionable site management advice.""".trimIndent()

        private fun truncate(value: String?, max: Int): String {
            if (value == null) {
                return ""
            }
            val trimmed = value.trim()
            return if (trimmed.length <= max) trimmed else trimmed.substring(0, max)
        }
    }
}
