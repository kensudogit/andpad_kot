package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CostEntryTest {

    @Test
    fun isRecordType() {
        assertThat(CostEntry::class.isData).isTrue()
    }

}
