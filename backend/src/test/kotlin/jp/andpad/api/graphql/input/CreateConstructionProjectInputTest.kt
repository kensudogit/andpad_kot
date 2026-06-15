package jp.andpad.api.graphql.input

import jp.andpad.api.domain.ConstructionProjectStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CreateConstructionProjectInputTest {

    @Test
    fun createsRecord() {
        val value = CreateConstructionProjectInput("P", "Addr", ConstructionProjectStatus.PLANNING, "Mgr", "2024-01-01", "2024-12-31")
        assertThat(value).isNotNull()
        assertThat(value::class.isData).isTrue()
    }

}
