package jp.andpad.api.graphql.input

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UpdateOrganizationInputTest {

    @Test
    fun createsRecord() {
        val value = UpdateOrganizationInput("New Name", "sample-construction", 10, "Asia/Tokyo")
        assertThat(value).isNotNull()
        assertThat(value::class.isData).isTrue()
    }

}
