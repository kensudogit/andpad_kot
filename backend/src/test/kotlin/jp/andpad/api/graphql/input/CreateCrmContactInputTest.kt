package jp.andpad.api.graphql.input

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CreateCrmContactInputTest {

    @Test
    fun createsRecord() {
        val value = CreateCrmContactInput("Co", "contact@x.jp", "090", "Company", "LEAD", "NOTE")
        assertThat(value).isNotNull()
        assertThat(value::class.isData).isTrue()
    }

}
