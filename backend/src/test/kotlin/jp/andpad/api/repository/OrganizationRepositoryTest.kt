package jp.andpad.api.repository

import jp.andpad.api.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class OrganizationRepositoryTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var organizationRepository: jp.andpad.api.repository.OrganizationRepository

    @Test
    fun demoOrganizationExists() {
        assertThat(organizationRepository.orgExists("org_demo")).isTrue()
    }

}
