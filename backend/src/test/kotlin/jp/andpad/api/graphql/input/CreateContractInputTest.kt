package jp.andpad.api.graphql.input

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CreateContractInputTest {

    @Test
    fun createsRecord() {
        val value = CreateContractInput("tmpl-1", "Title", "Party", "party@x.jp", "body")
        assertThat(value).isNotNull()
        assertThat(value::class.isData).isTrue()
    }

}
