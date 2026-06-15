package jp.andpad.api.auth.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RegisterRequestTest {

    @Test
    fun createsRecord() {
        val value = RegisterRequest("Clinic", "slug", "Owner", "a@b.jp", "pass1234")
        assertThat(value).isNotNull()
        assertThat(value::class.isData).isTrue()
    }

}
