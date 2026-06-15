package jp.andpad.api.config

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.util.StringUtils

/** Railway / ローカル向け PostgreSQL URL の解決と JDBC 正規化。 */
internal object DatabaseUrlSupport {
    private val USER_INFO = Pattern.compile("^(?:jdbc:)?postgres(?:ql)?://([^@]+@)")
    private val JDBC_USER_INFO = Pattern.compile("^(jdbc:postgresql://)[^@]+@(.+)$")

    fun resolveRawUrl(fallback: String): String {
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

    /** どの環境変数から DB URL を解決したか（/status 診断用）。 */
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

    fun normalizeForJdbc(raw: String?): String {
        if (!StringUtils.hasText(raw)) {
            return raw ?: ""
        }
        var url = raw!!.trim()

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

        url = stripUserInfoFromJdbcUrl(url)
        return appendSslModeIfMissing(url)
    }

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
        userInfo = userInfo.substring(0, userInfo.length - 1)
        val parts = userInfo.split(":", limit = 2)
        if (parts.isNotEmpty() && StringUtils.hasText(parts[0])) {
            props.username = decode(parts[0])
        }
        if (parts.size > 1) {
            props.password = decode(parts[1])
        }
    }

    private fun stripUserInfoFromJdbcUrl(jdbcUrl: String): String {
        val matcher = JDBC_USER_INFO.matcher(jdbcUrl)
        if (matcher.matches()) {
            return matcher.group(1) + matcher.group(2)
        }
        return jdbcUrl
    }

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
            dbName = "railway"
        }
        if (StringUtils.hasText(password)) {
            return "postgresql://${encode(user!!)}:${encode(password!!)}@$host:$port/$dbName"
        }
        return "postgresql://${encode(user!!)}@$host:$port/$dbName"
    }

    private fun firstEnv(vararg keys: String): String? {
        for (key in keys) {
            val value = System.getenv(key)
            if (StringUtils.hasText(value) && !value.contains("\${{")) {
                return value.trim()
            }
        }
        return null
    }

    private fun appendSslModeIfMissing(jdbcUrl: String): String {
        if (jdbcUrl.contains("sslmode=")) {
            return jdbcUrl
        }
        val mode = if (shouldDisableSsl(jdbcUrl)) "disable" else "require"
        return jdbcUrl + (if (jdbcUrl.contains("?")) "&" else "?") + "sslmode=$mode"
    }

    private fun shouldDisableSsl(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains(".railway.internal") ||
            lower.contains("localhost") ||
            lower.contains("127.0.0.1")
    }

    private fun decode(value: String): String = URLDecoder.decode(value, StandardCharsets.UTF_8)

    private fun encode(value: String): String = value.replace("@", "%40").replace(":", "%3A")
}
