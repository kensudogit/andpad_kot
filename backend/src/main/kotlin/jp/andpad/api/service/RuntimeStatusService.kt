package jp.andpad.api.service

import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import javax.sql.DataSource

/** /status 向けの DB 接続診断（秘密情報は含めない）。 */
@Service
class RuntimeStatusService(
    private val dataSource: DataSource,
) {

    fun isPostgresConnected(): Boolean {
        try {
            dataSource.connection.use { connection ->
                connection.createStatement().execute("SELECT 1")
                return true
            }
        } catch (_: Exception) {
            return false
        }
    }

    fun setupStatus(postgresConnected: Boolean): Map<String, Any> {
        val out = linkedMapOf<String, Any>()
        val dbSource = resolveDatabaseSource()
        out["postgres"] = postgresConnected
        out["databaseSource"] = if (StringUtils.hasText(dbSource)) dbSource else "none"
        out["databaseUrl"] = envPresence("DATABASE_URL")
        out["databasePrivateUrl"] = envPresence("DATABASE_PRIVATE_URL")
        out["pgHost"] = envPresence("PGHOST")
        out["jwtSecret"] = if (StringUtils.hasText(System.getenv("JWT_SECRET"))) "set" else "empty"
        out["openaiApiKey"] = envPresence("OPENAI_API_KEY")
        out["railway"] = isRailway()
        val publicDomain = System.getenv("RAILWAY_PUBLIC_DOMAIN")
        if (StringUtils.hasText(publicDomain) && !publicDomain.contains("\${{")) {
            out["publicUrl"] = "https://" + publicDomain.trim()
        }

        val jwtWarning = jwtSecretWarning(System.getenv("JWT_SECRET"))
        if (StringUtils.hasText(jwtWarning)) {
            out["jwtSecretWarning"] = jwtWarning
        }
        if (!postgresConnected && isRailway()) {
            out["hint"] =
                "andpad_j service → Variables → Reference → Postgres → DATABASE_URL. " +
                "JWT_SECRET = random string (not API key). Redeploy."
        }
        if (postgresConnected && "set" != envPresence("OPENAI_API_KEY")) {
            out["openaiHint"] = "AI チャットボット / AI Board 用に OPENAI_API_KEY を Variables に追加して Redeploy してください。"
        }
        return out
    }

    private fun resolveDatabaseSource(): String {
        for (key in arrayOf("DATABASE_URL", "DATABASE_PRIVATE_URL", "POSTGRES_URL", "POSTGRES_PRIVATE_URL")) {
            val value = System.getenv(key)
            if (StringUtils.hasText(value) && !value.contains("\${{")) {
                return key
            }
        }
        val pgHost = System.getenv("PGHOST")
        if (StringUtils.hasText(pgHost) && !pgHost.contains("\${{")) {
            return "PGHOST"
        }
        return ""
    }

    private fun isRailway(): Boolean {
        return StringUtils.hasText(System.getenv("RAILWAY_ENVIRONMENT")) ||
            StringUtils.hasText(System.getenv("RAILWAY_PROJECT_ID")) ||
            StringUtils.hasText(System.getenv("RAILWAY_SERVICE_ID"))
    }

    private fun envPresence(key: String): String {
        val raw = System.getenv(key)
        if (!StringUtils.hasText(raw)) {
            return "empty"
        }
        if (raw.contains("\${{")) {
            return "unresolved"
        }
        return "set"
    }

    private fun jwtSecretWarning(jwt: String?): String {
        if (!StringUtils.hasText(jwt)) {
            return "JWT_SECRET is empty"
        }
        val secret = jwt!!.trim()
        if (secret.startsWith("sk-ant") || secret.startsWith("sk-proj") || secret.startsWith("sk-")) {
            return "JWT_SECRET looks like an API key. Use a random string here; put OpenAI keys in OPENAI_API_KEY."
        }
        if ("dev-only-change-in-production-min-32-chars" == secret) {
            return "JWT_SECRET is still the dev default. Set a long random string on Railway."
        }
        return ""
    }
}
