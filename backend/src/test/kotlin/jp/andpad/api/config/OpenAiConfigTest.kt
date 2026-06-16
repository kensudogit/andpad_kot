package jp.andpad.api.config

import jp.andpad.api.AbstractIntegrationTest
import jp.andpad.api.openai.OpenAiClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class OpenAiConfigTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var openAiClient: OpenAiClient

    @Test
    fun openAiClientBeanIsRegistered() {
        assertThat(openAiClient).isNotNull()
    }

    @Test
    fun openAiClientIsDisabledWithoutApiKeyInTest() {
        assertThat(openAiClient.isEnabled()).isFalse()
    }
}
