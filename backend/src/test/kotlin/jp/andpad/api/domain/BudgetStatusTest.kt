package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BudgetStatusTest {

    @Test
    fun valuesAreDefined() {
        assertThat(BudgetStatus.entries).isNotEmpty()
    }

}
