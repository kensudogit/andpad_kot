package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AttendanceRecordTest {

    @Test
    fun isRecordType() {
        assertThat(AttendanceRecord::class.isData).isTrue()
    }

}
