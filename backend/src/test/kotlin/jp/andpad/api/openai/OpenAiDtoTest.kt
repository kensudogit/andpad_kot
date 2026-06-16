package jp.andpad.api.openai

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OpenAiDtoTest {

    private val mapper = jacksonObjectMapper()

    @Test
    fun serializesChatRequest() {
        val request = OpenAiChatRequest(
            "gpt-4o-mini",
            listOf(OpenAiChatMessage("user", "hello")),
        )

        val json = mapper.writeValueAsString(request)

        assertThat(json).contains("gpt-4o-mini")
        assertThat(json).contains("hello")
    }

    @Test
    fun deserializesChatResponse() {
        val json = """
            {
              "choices": [
                { "message": { "role": "assistant", "content": "ok" } }
              ]
            }
        """.trimIndent()

        val response = mapper.readValue(json, OpenAiChatResponse::class.java)

        assertThat(response.choices).hasSize(1)
        assertThat(response.choices!![0].message?.content).isEqualTo("ok")
        assertThat(response.error).isNull()
    }

    @Test
    fun deserializesErrorField() {
        val json = """{ "choices": [], "error": { "message": "rate limited" } }"""

        val response = mapper.readValue(json, OpenAiChatResponse::class.java)

        assertThat(response.error?.message).isEqualTo("rate limited")
    }
}
