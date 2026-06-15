package jp.andpad.api.openai

/** OpenAI Chat Completions の1メッセージ。 */
data class OpenAiChatMessage(val role: String, val content: String)
