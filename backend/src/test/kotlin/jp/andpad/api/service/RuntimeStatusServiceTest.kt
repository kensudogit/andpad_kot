package jp.andpad.api.service

import jp.andpad.api.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class RuntimeStatusServiceTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var runtimeStatusService: RuntimeStatusService

    @Test
    fun postgresConnected() {
        assertThat(runtimeStatusService.isPostgresConnected()).isTrue()
    }

}
