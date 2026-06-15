package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ProjectBudgetTest {

    @Test
    fun isRecordType() {
        assertThat(ProjectBudget::class.isData).isTrue()
    }

}
