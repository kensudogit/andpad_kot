/**
 * Railway / ローカル PostgreSQL 接続 URL の解決と JDBC 正規化ユーティリティ。
 * `DATABASE_URL` 等の環境変数や PGHOST 系コンポーネントから接続文字列を組み立て、
 * `postgresql://` を `jdbc:postgresql://` に変換し SSL モードを付与する。
 * [DataSourceConfig] から DataSource 初期化時に呼び出される。
 */
package jp.andpad.api.config

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.util.StringUtils

/**
 * PostgreSQL 接続 URL の環境変数解決・JDBC 正規化を行う内部オブジェクト。
 *
 * - [USER_INFO]: URL 内 user:password@ 部分の抽出用正規表現
 * - [JDBC_USER_INFO]: JDBC URL から認証情報を除去する正規表現
 */
internal object DatabaseUrlSupport {
    private val USER_INFO = Pattern.compile("^(?:jdbc:)?postgres(?:ql)?://([^@]+@)")
    private val JDBC_USER_INFO = Pattern.compile("^(jdbc:postgresql://)[^@]+@(.+)$")

    /**
     * 環境変数または PG コンポーネントから生の DB URL を解決する。
     * Railway 未展開プレースホルダ（`${{`）は無視する。
     *
     * @param fallback application.yml の spring.datasource.url
     * @return 使用する接続 URL 文字列
     */
    fun resolveRawUrl(fallback: String): String {
        // Railway が提供する URL 系変数を優先順に探索
        for (key in arrayOf("DATABASE_URL", "DATABASE_PRIVATE_URL", "POSTGRES_URL", "POSTGRES_PRIVATE_URL")) {
            val value = System.getenv(key)
            if (StringUtils.hasText(value) && !value.contains("\${{")) {
                return value.trim()
            }
        }
        val fromComponents = fromPgComponents()
        if (fromComponents != null) {
            return fromComponents
        }
        return fallback
    }

    /**
     * どの環境変数から DB URL を解決したかを返す（/status 診断用）。
     *
     * @return 使用した環境変数名、コンポーネント合成時は "PGHOST"、未解決時は空文字
     */
    fun resolveDatabaseSource(): String {
        for (key in arrayOf("DATABASE_URL", "DATABASE_PRIVATE_URL", "POSTGRES_URL", "POSTGRES_PRIVATE_URL")) {
            val value = System.getenv(key)
            if (StringUtils.hasText(value) && !value.contains("\${{")) {
                return key
            }
        }
        if (fromPgComponents() != null) {
            return "PGHOST"
        }
        return ""
    }

    /**
     * 文字列が PostgreSQL 接続 URL 形式か判定する。
     *
     * @param raw 接続 URL 候補
     * @return postgres(ql):// または jdbc:postgres で始まれば true
     */
    fun isPostgresUrl(raw: String?): Boolean {
        if (!StringUtils.hasText(raw)) {
            return false
        }
        val lower = raw!!.trim().lowercase()
        return lower.startsWith("postgresql://") ||
            lower.startsWith("postgres://") ||
            lower.startsWith("jdbc:postgresql://") ||
            lower.startsWith("jdbc:postgres://")
    }

    /**
     * Spring JDBC / HikariCP 向けに URL を正規化する。
     * スキーム統一、URL 内認証情報の除去、sslmode 付与を行う。
     *
     * @param raw 生の接続 URL
     * @return `jdbc:postgresql://...` 形式の URL
     */
    fun normalizeForJdbc(raw: String?): String {
        if (!StringUtils.hasText(raw)) {
            return raw ?: ""
        }
        var url = raw!!.trim()

        // postgres:// と jdbc:postgres:// を jdbc:postgresql:// に統一
        if (url.startsWith("jdbc:postgres://")) {
            url = "jdbc:postgresql://" + url.substring("jdbc:postgres://".length)
        } else if (url.startsWith("postgres://")) {
            url = "postgresql://" + url.substring("postgres://".length)
        }

        if (url.startsWith("postgresql://")) {
            url = "jdbc:$url"
        }

        if (!url.startsWith("jdbc:postgresql://")) {
            return url
        }

        // Hikari は username/password を別プロパティで渡すため URL から userinfo を除去
        url = stripUserInfoFromJdbcUrl(url)
        return appendSslModeIfMissing(url)
    }

    /**
     * URL 内の user:password を DataSourceProperties の username/password に設定する。
     * 環境変数 DB_USER / DB_PASSWORD が既にあれば上書きしない。
     *
     * @param props Spring DataSource プロパティ（in/out）
     * @param rawUrl 認証情報を含む可能性のある生 URL
     */
    fun applyCredentialsFromUrl(props: DataSourceProperties, rawUrl: String) {
        if (StringUtils.hasText(System.getenv("DB_USER")) || StringUtils.hasText(System.getenv("DB_PASSWORD"))) {
            return
        }
        val matcher = USER_INFO.matcher(rawUrl.trim())
        if (!matcher.find()) {
            return
        }
        var userInfo = matcher.group(1)
        if (!StringUtils.hasText(userInfo)) {
            return
        }
        userInfo = userInfo.substring(0, userInfo.length - 1) // 末尾 @ を除去
        val parts = userInfo.split(":", limit = 2)
        if (parts.isNotEmpty() && StringUtils.hasText(parts[0])) {
            props.username = decode(parts[0])
        }
        if (parts.size > 1) {
            props.password = decode(parts[1])
        }
    }

    /**
     * JDBC URL から userinfo 部分（user:pass@）を除去する。
     *
     * @param jdbcUrl `jdbc:postgresql://user:pass@host/db` 形式
     * @return 認証情報除去後の JDBC URL
     */
    private fun stripUserInfoFromJdbcUrl(jdbcUrl: String): String {
        val matcher = JDBC_USER_INFO.matcher(jdbcUrl)
        if (matcher.matches()) {
            return matcher.group(1) + matcher.group(2)
        }
        return jdbcUrl
    }

    /**
     * PGHOST / PGUSER 等の個別環境変数から postgresql:// URL を組み立てる。
     *
     * @return 合成 URL、host または user が無ければ null
     */
    private fun fromPgComponents(): String? {
        val host = firstEnv("PGHOST", "POSTGRES_HOST")
        val user = firstEnv("PGUSER", "POSTGRES_USER")
        val password = firstEnv("PGPASSWORD", "POSTGRES_PASSWORD")
        var dbName = firstEnv("PGDATABASE", "POSTGRES_DB", "POSTGRES_DATABASE")
        var port = firstEnv("PGPORT", "POSTGRES_PORT")
        if (!StringUtils.hasText(host) || !StringUtils.hasText(user)) {
            return null
        }
        if (!StringUtils.hasText(port)) {
            port = "5432"
        }
        if (!StringUtils.hasText(dbName)) {
            dbName = "railway" // Railway デフォルト DB 名
        }
        if (StringUtils.hasText(password)) {
            return "postgresql://${encode(user!!)}:${encode(password!!)}@$host:$port/$dbName"
        }
        return "postgresql://${encode(user!!)}@$host:$port/$dbName"
    }

    /**
     * 複数キーのうち最初に有効な環境変数値を返す。
     *
     * @param keys 探索する環境変数名（優先順）
     * @return トリム済み値、無ければ null
     */
    private fun firstEnv(vararg keys: String): String? {
        for (key in keys) {
            val value = System.getenv(key)
            if (StringUtils.hasText(value) && !value.contains("\${{")) {
                return value.trim()
            }
        }
        return null
    }

    /**
     * sslmode クエリパラメータが無い場合に付与する。
     * 内部ネットワーク・localhost は disable、それ以外は require。
     *
     * @param jdbcUrl 正規化済み JDBC URL
     * @return sslmode 付き URL
     */
    private fun appendSslModeIfMissing(jdbcUrl: String): String {
        if (jdbcUrl.contains("sslmode=")) {
            return jdbcUrl
        }
        val mode = if (shouldDisableSsl(jdbcUrl)) "disable" else "require"
        return jdbcUrl + (if (jdbcUrl.contains("?")) "&" else "?") + "sslmode=$mode"
    }

    /**
     * Railway 内部 DNS や localhost では SSL を無効化する。
     *
     * @param url JDBC 接続 URL
     * @return SSL 不要と判断できる場合 true
     */
    private fun shouldDisableSsl(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains(".railway.internal") ||
            lower.contains("localhost") ||
            lower.contains("127.0.0.1")
    }

    /** URL エンコード済み userinfo をデコードする。 */
    private fun decode(value: String): String = URLDecoder.decode(value, StandardCharsets.UTF_8)

    /** パスワード内の @ や : を URL エスケープする簡易エンコード。 */
    private fun encode(value: String): String = value.replace("@", "%40").replace(":", "%3A")
}
