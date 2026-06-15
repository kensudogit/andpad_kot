package jp.andpad.api.repository

import jp.andpad.api.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SaasRepositoryTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var saasRepository: jp.andpad.api.repository.SaasRepository

    @Test
    fun listsModules() {
        assertThat(saasRepository.listOrgModules("org_demo")).isNotEmpty()
    }

}
