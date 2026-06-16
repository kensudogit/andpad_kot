package jp.andpad.api.openai

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OpenAiChatMessageTest {

    @Test
    fun createsRecord() {
        val message = OpenAiChatMessage("user", "hello")

        assertThat(message.role).isEqualTo("user")
        assertThat(message.content).isEqualTo("hello")
        assertThat(message::class.isData).isTrue()
    }

    @Test
    fun copyPreservesValues() {
        val original = OpenAiChatMessage("assistant", "answer")
        val copied = original.copy(content = "updated")

        assertThat(copied.role).isEqualTo("assistant")
        assertThat(copied.content).isEqualTo("updated")
    }
}
