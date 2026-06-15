package jp.andpad.api.graphql.input

import jp.andpad.api.domain.SaasModuleCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CreateProjectModuleRecordInputTest {

    @Test
    fun createsRecord() {
        val value = CreateProjectModuleRecordInput("prj-demo-1", SaasModuleCode.BILLING, "Title", "OPEN", "detail", 100.0, "Person", "2024-01-01")
        assertThat(value).isNotNull()
        assertThat(value::class.isData).isTrue()
    }

}
