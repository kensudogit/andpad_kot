/**
 * ANDPAD 建設 PM SaaS API の Spring Boot エントリポイント。
 * アプリケーション起動時にコンポーネントスキャンと DI コンテナを初期化し、
 * REST / GraphQL / セキュリティ / データソース設定を一括で読み込む。
 * [AppProperties] により JWT シークレットや TTL などの実行時設定をバインドする。
 */
package jp.andpad.api

import jp.andpad.api.config.AppProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

/**
 * Spring Boot アプリケーション本体。
 * 追加の Bean 定義は各 `@Configuration` クラス（SecurityConfig, DataSourceConfig 等）に委譲する。
 */
@SpringBootApplication
@EnableConfigurationProperties(AppProperties::class)
class AndpadApplication

/**
 * JVM プロセスのメイン関数。
 *
 * @param args コマンドライン引数（Spring Boot 標準）
 */
fun main(args: Array<String>) {
    runApplication<AndpadApplication>(*args)
}
