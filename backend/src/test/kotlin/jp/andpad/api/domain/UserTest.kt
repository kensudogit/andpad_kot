package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserTest {

    @Test
    fun createsRecord() {
        val value = User("u1", "a@b.jp", "Name", "")
        assertThat(value).isNotNull()
        assertThat(value::class.isData).isTrue()
    }

}
