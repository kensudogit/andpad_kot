/**
 * OpenAI Chat Completions API クライアント。
 * 建設 PM 向け AI 相談・分析機能から REST で `/v1/chat/completions` を呼び出し、
 * システムプロンプト・履歴・ユーザーメッセージを組み立てて回答テキストを返す。
 * [OpenAiProperties] と環境変数 OPENAI_API_KEY に依存する。
 */
package jp.andpad.api.openai

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

/**
 * OpenAI Chat Completions 呼び出しコンポーネント。
 *
 * @property apiKey OPENAI API キー（未設定時は [isEnabled] が false）
 * @property model 使用モデル名（未設定時 gpt-4o-mini）
 * @property http OpenAI API 向け RestClient
 */
@Component
class OpenAiClient(properties: OpenAiProperties) {

    private val log = LoggerFactory.getLogger(OpenAiClient::class.java)
    private val apiKey: String = properties.apiKey?.trim().orEmpty()
    private val model: String = if (StringUtils.hasText(properties.model)) properties.model!!.trim() else "gpt-4o-mini"
    private val http: RestClient = RestClient.builder()
        .baseUrl("https://api.openai.com")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()

    /**
     * API キーが設定されているか判定する。
     *
     * @return キーが非空なら true
     */
    fun isEnabled(): Boolean = StringUtils.hasText(apiKey)

    /**
     * Chat Completions API で会話を実行し、assistant の回答テキストを返す。
     *
     * @param systemPrompt システムロールの指示（建設 PM 向けペルソナ等）
     * @param history 過去の user/assistant メッセージ（null 可、空 content は除外）
     * @param userMessage 今回のユーザー入力
     * @return トリム済み回答文字列
     * @throws IllegalStateException API キー未設定、HTTP エラー、空レスポンス時
     */
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

            // API が 200 でも body.error にメッセージが載る場合がある
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
        /** 建設 PM 相談チャット用のシステムプロンプト（日本語応答・安全上の注意付き）。 */
        val CONSTRUCTION_CONSULT_SYSTEM: String = """
            You are an AI assistant for construction project management professionals in Japan (ANDPAD-style platform).
            Help with site safety, schedule coordination, subcontractor management, quality inspection, document workflows, BIM/digital delivery, and general construction PM best practices.
            Be practical and concise. When unsure, say what information is needed and suggest checking site rules or consulting a qualified supervisor.
            Do not provide legally binding engineering sign-off. Respond in Japanese unless the user writes in another language.""".trimIndent()

        /** KPI JSON を入力とする分析ボード用システムプロンプト（JSON のみ出力）。 */
        val CONSTRUCTION_ANALYTICS_SYSTEM: String = """
            You are a construction project management analyst (AI Board). Given JSON analytics KPIs for a construction PM platform, respond ONLY with valid JSON:
            {"summary":"...","strengths":["..."],"risks":["..."],"recommendations":["..."]}
            Write in Japanese. Focus on project delivery, schedule risk, module adoption, billing trends, safety/compliance awareness, and actionable site management advice.""".trimIndent()

        /**
         * ログ・例外メッセージ用に文字列を最大長で切り詰める。
         *
         * @param value 入力（null は空文字）
         * @param max 最大文字数
         * @return 切り詰め後の文字列
         */
        private fun truncate(value: String?, max: Int): String {
            if (value == null) {
                return ""
            }
            val trimmed = value.trim()
            return if (trimmed.length <= max) trimmed else trimmed.substring(0, max)
        }
    }
}
