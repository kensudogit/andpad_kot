package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CrmContactTest {

    @Test
    fun isRecordType() {
        assertThat(CrmContact::class.isData).isTrue()
    }

}
