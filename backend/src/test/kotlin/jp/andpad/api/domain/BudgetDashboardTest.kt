package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BudgetDashboardTest {

    @Test
    fun isRecordType() {
        assertThat(BudgetDashboard::class.isData).isTrue()
    }

}
