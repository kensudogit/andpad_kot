package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BudgetTypeTest {

    @Test
    fun valuesAreDefined() {
        assertThat(BudgetType.entries).isNotEmpty()
    }

}
