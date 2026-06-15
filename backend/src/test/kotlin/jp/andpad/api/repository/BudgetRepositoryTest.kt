package jp.andpad.api.repository

import jp.andpad.api.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BudgetRepositoryTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var budgetRepository: jp.andpad.api.repository.BudgetRepository

    @Test
    fun listsBudgets() {
        assertThat(budgetRepository.listBudgets("org_demo", "prj-demo-1", null)).isNotEmpty()
    }

}
