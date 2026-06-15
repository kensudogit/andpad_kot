package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CrmInteractionTest {

    @Test
    fun createsRecord() {
        val value = CrmInteraction("i1", "c1", "CALL", "summary", "2024-01-01")
        assertThat(value).isNotNull()
        assertThat(value::class.isData).isTrue()
    }

}
