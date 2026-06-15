package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SessionTest {

    @Test
    fun isRecordType() {
        assertThat(Session::class.isData).isTrue()
    }

}
