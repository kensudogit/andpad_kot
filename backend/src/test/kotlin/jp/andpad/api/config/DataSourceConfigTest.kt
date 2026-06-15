package jp.andpad.api.config

import jp.andpad.api.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class DataSourceConfigTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var dataSource: javax.sql.DataSource

    @Test
    fun dataSourceConnects()  {
        dataSource.connection.use { c ->
            assertThat(c.isValid(2)).isTrue()
        }
    }

}
