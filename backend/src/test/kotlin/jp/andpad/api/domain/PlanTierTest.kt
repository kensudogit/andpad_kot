package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PlanTierTest {

    @Test
    fun valuesAreDefined() {
        assertThat(PlanTier.entries).isNotEmpty()
    }

}
