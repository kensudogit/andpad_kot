package jp.andpad.api.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AppPropertiesTest {

    @Test
    fun createsRecord() {
        val value = AppProperties(AppProperties.Jwt("secret-min-32-chars-long-enough", 72))
        assertThat(value).isNotNull()
        assertThat(value::class.isData).isTrue()
    }

}
