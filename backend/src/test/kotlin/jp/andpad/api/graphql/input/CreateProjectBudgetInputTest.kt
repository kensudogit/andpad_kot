package jp.andpad.api.graphql.input

import jp.andpad.api.domain.BudgetStatus
import jp.andpad.api.domain.BudgetType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CreateProjectBudgetInputTest {

    @Test
    fun createsRecord() {
        val value = CreateProjectBudgetInput("prj-demo-1", "Budget", BudgetType.EXECUTION_BUDGET, BudgetStatus.DRAFT, 1, 1000.0, "notes")
        assertThat(value).isNotNull()
        assertThat(value::class.isData).isTrue()
    }

}
