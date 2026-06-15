package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LeaveRequestTest {

    @Test
    fun isRecordType() {
        assertThat(LeaveRequest::class.isData).isTrue()
    }

}
