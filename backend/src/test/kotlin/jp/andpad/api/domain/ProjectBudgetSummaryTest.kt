package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ProjectBudgetSummaryTest {

    @Test
    fun isRecordType() {
        assertThat(ProjectBudgetSummary::class.isData).isTrue()
    }

}
