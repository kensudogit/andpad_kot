package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TeamMemberTest {

    @Test
    fun isRecordType() {
        assertThat(TeamMember::class.isData).isTrue()
    }

}
