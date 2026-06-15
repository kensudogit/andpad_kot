package jp.andpad.api.openai

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class OpenAiChatRequest(val model: String, val messages: List<OpenAiChatMessage>)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class OpenAiChatResponse(val choices: List<OpenAiChoice>?, val error: OpenAiError?)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class OpenAiChoice(val message: OpenAiChatMessage?)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class OpenAiError(val message: String?)
