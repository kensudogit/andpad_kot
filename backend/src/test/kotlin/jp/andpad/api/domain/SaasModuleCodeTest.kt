package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SaasModuleCodeTest {

    @Test
    fun valuesAreDefined() {
        assertThat(SaasModuleCode.entries).isNotEmpty()
    }

}
