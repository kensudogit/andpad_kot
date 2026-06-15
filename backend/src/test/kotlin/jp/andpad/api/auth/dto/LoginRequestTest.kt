package jp.andpad.api.auth.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LoginRequestTest {

    @Test
    fun createsRecord() {
        val value = LoginRequest("demo@sakura-dental.jp", "demo1234")
        assertThat(value).isNotNull()
        assertThat(value::class.isData).isTrue()
    }

}
