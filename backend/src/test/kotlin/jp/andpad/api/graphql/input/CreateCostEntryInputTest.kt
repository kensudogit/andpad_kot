package jp.andpad.api.graphql.input

import jp.andpad.api.domain.CostEntryType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CreateCostEntryInputTest {

    @Test
    fun createsRecord() {
        val value = CreateCostEntryInput("prj-demo-1", "bli-demo-1", CostEntryType.MATERIAL, "Vendor", "desc", 1000.0, "2024-01-01", "INV-1", "recorder")
        assertThat(value).isNotNull()
        assertThat(value::class.isData).isTrue()
    }

}
