package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MonthlyCostMetricTest {

    @Test
    fun createsRecord() {
        val value = MonthlyCostMetric("2024-01", 1000.0)
        assertThat(value).isNotNull()
        assertThat(value::class.isData).isTrue()
    }

}
