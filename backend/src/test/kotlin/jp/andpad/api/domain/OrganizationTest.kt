package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OrganizationTest {

    @Test
    fun isRecordType() {
        assertThat(Organization::class.isData).isTrue()
    }

}
