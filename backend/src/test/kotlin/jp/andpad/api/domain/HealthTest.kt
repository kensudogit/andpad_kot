package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HealthTest {

    @Test
    fun createsRecord() {
        val value = Health(true, "andpad-api", "1.0")
        assertThat(value).isNotNull()
        assertThat(value::class.isData).isTrue()
    }

}
