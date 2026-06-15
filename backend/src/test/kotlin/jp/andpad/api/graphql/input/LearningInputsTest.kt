package jp.andpad.api.graphql.input

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LearningInputsTest {

    @Test
    fun nestedInputsConstruct() {
        val input = LearningInputs.UpdateWatchProgressInput("v-1", "user_demo", 30, false)
        assertThat(input.videoId).isEqualTo("v-1")
    }

}
