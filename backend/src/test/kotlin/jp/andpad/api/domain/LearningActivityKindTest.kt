package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LearningActivityKindTest {

    @Test
    fun valuesAreDefined() {
        assertThat(LearningActivityKind.entries).isNotEmpty()
    }

}
