package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MemberRoleTest {

    @Test
    fun valuesAreDefined() {
        assertThat(MemberRole.entries).isNotEmpty()
    }

}
