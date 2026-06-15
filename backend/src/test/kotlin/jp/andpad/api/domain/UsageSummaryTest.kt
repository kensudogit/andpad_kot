package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UsageSummaryTest {

    @Test
    fun createsRecord() {
        val value = UsageSummary(1, 10, 2, 100, 0, 1000, 0)
        assertThat(value).isNotNull()
        assertThat(value::class.isData).isTrue()
    }

}
