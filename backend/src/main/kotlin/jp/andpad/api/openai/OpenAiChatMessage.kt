/**
 * OpenAI Chat Completions の 1 メッセージ DTO。
 * role（system/user/assistant）と content を保持し、
 * [OpenAiClient] の会話履歴および API リクエスト/レスポンスで使用する。
 */
package jp.andpad.api.openai

/**
 * Chat Completions の単一メッセージ。
 *
 * @property role メッセージロール（system / user / assistant）
 * @property content 本文テキスト
 */
data class OpenAiChatMessage(val role: String, val content: String)
