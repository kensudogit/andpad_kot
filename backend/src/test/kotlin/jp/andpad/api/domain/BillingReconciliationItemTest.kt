package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BillingReconciliationItemTest {

    @Test
    fun isRecordType() {
        assertThat(BillingReconciliationItem::class.isData).isTrue()
    }

}
