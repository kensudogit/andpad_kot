package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CostEntryTypeTest {

    @Test
    fun valuesAreDefined() {
        assertThat(CostEntryType.entries).isNotEmpty()
    }

}
