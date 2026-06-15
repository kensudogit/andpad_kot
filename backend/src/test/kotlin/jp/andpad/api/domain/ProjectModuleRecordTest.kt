package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ProjectModuleRecordTest {

    @Test
    fun isRecordType() {
        assertThat(ProjectModuleRecord::class.isData).isTrue()
    }

}
