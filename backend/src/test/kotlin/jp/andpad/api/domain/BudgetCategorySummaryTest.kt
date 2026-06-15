package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BudgetCategorySummaryTest {

    @Test
    fun isRecordType() {
        assertThat(BudgetCategorySummary::class.isData).isTrue()
    }

}
