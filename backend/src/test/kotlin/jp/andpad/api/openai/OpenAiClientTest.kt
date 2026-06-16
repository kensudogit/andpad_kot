package jp.andpad.api.openai

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class OpenAiClientTest {

    @Test
    fun isDisabledWhenApiKeyMissing() {
        val client = OpenAiClient(OpenAiProperties(null, null))

        assertThat(client.isEnabled()).isFalse()
    }

    @Test
    fun isDisabledWhenApiKeyBlank() {
        val client = OpenAiClient(OpenAiProperties("   ", null))

        assertThat(client.isEnabled()).isFalse()
    }

    @Test
    fun isEnabledWhenApiKeyPresent() {
        val client = OpenAiClient(OpenAiProperties("sk-test-key", "gpt-4o-mini"))

        assertThat(client.isEnabled()).isTrue()
    }

    @Test
    fun chatThrowsWhenApiKeyNotConfigured() {
        val client = OpenAiClient(OpenAiProperties(null, null))

        assertThatThrownBy { client.chat("system", null, "hello") }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("OPENAI_API_KEY")
    }

    @Test
    fun systemPromptsAreDefined() {
        assertThat(OpenAiClient.CONSTRUCTION_CONSULT_SYSTEM).contains("construction")
        assertThat(OpenAiClient.CONSTRUCTION_ANALYTICS_SYSTEM).contains("JSON")
    }
}
