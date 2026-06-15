package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SaasModuleTest {

    @Test
    fun createsRecord() {
        val value = SaasModule(SaasModuleCode.CONSTRUCTION_MGMT, "施工", "desc", true)
        assertThat(value).isNotNull()
        assertThat(value::class.isData).isTrue()
    }

}
