package jp.andpad.api.openai

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.openai")
data class OpenAiProperties(val apiKey: String?, val model: String?)
