package jp.andpad.api.graphql.input

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CreateLeaveRequestInputTest {

    @Test
    fun createsRecord() {
        val value = CreateLeaveRequestInput("2024-01-01", "2024-01-02", "reason")
        assertThat(value).isNotNull()
        assertThat(value::class.isData).isTrue()
    }

}
