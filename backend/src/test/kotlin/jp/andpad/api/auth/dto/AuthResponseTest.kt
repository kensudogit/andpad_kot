package jp.andpad.api.auth.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AuthResponseTest {

    @Test
    fun isRecordType() {
        assertThat(AuthResponse::class.isData).isTrue()
    }

}
