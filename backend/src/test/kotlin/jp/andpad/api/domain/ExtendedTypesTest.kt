package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ExtendedTypesTest {

    @Test
    fun nestedRecordsConstruct() {
        val kpi = ExtendedTypes.AndpadAnalyticsKpi("Projects", 3.0, "件", 1.0)
        assertThat(kpi.label).isEqualTo("Projects")
        val doc = ExtendedTypes.RagDocument("r1", "t", "c", listOf(), "2024-01-01")
        assertThat(doc.id).isEqualTo("r1")
    }

}
