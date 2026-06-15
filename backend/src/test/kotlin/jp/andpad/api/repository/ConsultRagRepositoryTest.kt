package jp.andpad.api.repository

import jp.andpad.api.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ConsultRagRepositoryTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var consultRagRepository: jp.andpad.api.repository.ConsultRagRepository

    @Test
    fun listsRagDocuments() {
        assertThat(consultRagRepository.listRagDocuments("org_demo")).isNotEmpty()
    }

}
