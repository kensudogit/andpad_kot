package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SkillLevelTest {

    @Test
    fun valuesAreDefined() {
        assertThat(SkillLevel.entries).isNotEmpty()
    }

}
