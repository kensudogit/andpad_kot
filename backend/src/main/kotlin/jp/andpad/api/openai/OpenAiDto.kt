/**
 * OpenAI Chat Completions API のリクエスト/レスポンス DTO。
 * Jackson で JSON シリアライズ/デシリアライズし、
 * [OpenAiClient] が HTTP ボディとして送受信する。未知フィールドは無視する。
 */
package jp.andpad.api.openai

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * POST /v1/chat/completions のリクエストボディ。
 *
 * @property model 使用モデル ID
 * @property messages ロール付きメッセージ列
 */
@JsonIgnoreProperties(ignoreUnknown = true)
internal data class OpenAiChatRequest(val model: String, val messages: List<OpenAiChatMessage>)

/**
 * Chat Completions API のレスポンス本体。
 *
 * @property choices 生成候補リスト（通常先頭を使用）
 * @property error API エラー情報（HTTP 200 でも設定される場合あり）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
internal data class OpenAiChatResponse(val choices: List<OpenAiChoice>?, val error: OpenAiError?)

/**
 * 1 つの生成候補。
 *
 * @property message assistant ロールのメッセージ
 */
@JsonIgnoreProperties(ignoreUnknown = true)
internal data class OpenAiChoice(val message: OpenAiChatMessage?)

/**
 * OpenAI API エラー詳細。
 *
 * @property message 人間可読なエラーメッセージ
 */
@JsonIgnoreProperties(ignoreUnknown = true)
internal data class OpenAiError(val message: String?)
