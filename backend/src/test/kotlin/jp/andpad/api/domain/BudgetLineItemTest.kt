package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BudgetLineItemTest {

    @Test
    fun isRecordType() {
        assertThat(BudgetLineItem::class.isData).isTrue()
    }

}
