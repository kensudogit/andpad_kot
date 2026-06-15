package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ConstructionProjectStatusTest {

    @Test
    fun valuesAreDefined() {
        assertThat(ConstructionProjectStatus.entries).isNotEmpty()
    }

}
