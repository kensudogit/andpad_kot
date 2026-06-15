package jp.andpad.api.config

import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

/** Railway の postgresql:// URL を Spring JDBC 向け jdbc:postgresql:// に正規化する。 */
@Configuration
class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    fun dataSourceProperties(): DataSourceProperties = DataSourceProperties()

    @Bean
    @Primary
    fun dataSource(props: DataSourceProperties): DataSource {
        val raw = DatabaseUrlSupport.resolveRawUrl(props.url)
        if (DatabaseUrlSupport.isPostgresUrl(raw)) {
            DatabaseUrlSupport.applyCredentialsFromUrl(props, raw)
            props.url = DatabaseUrlSupport.normalizeForJdbc(raw)
        }
        return props.initializeDataSourceBuilder().type(HikariDataSource::class.java).build()
    }
}
