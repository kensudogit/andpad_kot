package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContractTemplateTest {

    @Test
    fun createsRecord() {
        val value = ContractTemplate("t1", "Name", "body", "2024-01-01")
        assertThat(value).isNotNull()
        assertThat(value::class.isData).isTrue()
    }

}
