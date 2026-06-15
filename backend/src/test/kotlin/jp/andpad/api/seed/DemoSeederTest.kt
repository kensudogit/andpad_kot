package jp.andpad.api.seed

import jp.andpad.api.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class DemoSeederTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var demoSeeder: DemoSeeder

    @Test
    fun seederBeanLoads() {
        assertThat(demoSeeder).isNotNull()
    }

}
