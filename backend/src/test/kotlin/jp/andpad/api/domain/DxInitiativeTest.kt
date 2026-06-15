package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DxInitiativeTest {

    @Test
    fun isRecordType() {
        assertThat(DxInitiative::class.isData).isTrue()
    }

}
