/**
 * HikariCP データソース Bean 定義。
 * application.yml の spring.datasource 設定をベースに、
 * [DatabaseUrlSupport] で Railway 環境の PostgreSQL URL を JDBC 形式へ正規化して接続する。
 */
package jp.andpad.api.config

import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

/**
 * プライマリ [DataSource] と [DataSourceProperties] を提供する設定クラス。
 */
@Configuration
class DataSourceConfig {

    /**
     * spring.datasource プレフィックスのプロパティ Bean。
     *
     * @return バインド対象の [DataSourceProperties]
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    fun dataSourceProperties(): DataSourceProperties = DataSourceProperties()

    /**
     * Railway / ローカル向けに URL を正規化した HikariCP データソースを構築する。
     *
     * @param props spring.datasource からバインドされたプロパティ
     * @return 接続プール付き [DataSource]
     */
    @Bean
    @Primary
    fun dataSource(props: DataSourceProperties): DataSource {
        val raw = DatabaseUrlSupport.resolveRawUrl(props.url)
        if (DatabaseUrlSupport.isPostgresUrl(raw)) {
            // URL 埋め込み credentials を props.username/password へ展開
            DatabaseUrlSupport.applyCredentialsFromUrl(props, raw)
            props.url = DatabaseUrlSupport.normalizeForJdbc(raw)
        }
        return props.initializeDataSourceBuilder().type(HikariDataSource::class.java).build()
    }
}
