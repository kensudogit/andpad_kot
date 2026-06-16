package jp.andpad.api.openai

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OpenAiPropertiesTest {

    @Test
    fun createsRecord() {
        val properties = OpenAiProperties("sk-test", "gpt-4o-mini")

        assertThat(properties.apiKey).isEqualTo("sk-test")
        assertThat(properties.model).isEqualTo("gpt-4o-mini")
        assertThat(properties::class.isData).isTrue()
    }

    @Test
    fun allowsNullFields() {
        val properties = OpenAiProperties(null, null)

        assertThat(properties.apiKey).isNull()
        assertThat(properties.model).isNull()
    }
}
