package jp.andpad.api.graphql.input

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CreateDxInitiativeInputTest {

    @Test
    fun createsRecord() {
        val value = CreateDxInitiativeInput("DX", "desc", "PLANNED", 0, "Owner", "2024-12-31")
        assertThat(value).isNotNull()
        assertThat(value::class.isData).isTrue()
    }

}
