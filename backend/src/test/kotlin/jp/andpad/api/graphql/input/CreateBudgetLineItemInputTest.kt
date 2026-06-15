package jp.andpad.api.graphql.input

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CreateBudgetLineItemInputTest {

    @Test
    fun createsRecord() {
        val value = CreateBudgetLineItemInput("bud-demo-1", "DIRECT", "直接", "WBS", "desc", 100.0, 100.0, 0.0, 1)
        assertThat(value).isNotNull()
        assertThat(value::class.isData).isTrue()
    }

}
