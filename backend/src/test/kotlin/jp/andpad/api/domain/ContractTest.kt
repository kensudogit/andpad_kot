package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContractTest {

    @Test
    fun isRecordType() {
        assertThat(Contract::class.isData).isTrue()
    }

}
