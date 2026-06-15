package jp.andpad.api.security

import java.util.Optional
import org.springframework.security.core.context.SecurityContextHolder

object TenantContext {
    const val DEMO_ORG_ID: String = "org_demo"
    const val DEMO_USER_ID: String = "user_demo"

    fun principal(): Optional<AuthPrincipal> {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null && auth.principal is AuthPrincipal) {
            return Optional.of(auth.principal as AuthPrincipal)
        }
        return Optional.empty()
    }

    fun orgId(): String = principal().map { it.orgId }.orElse(DEMO_ORG_ID)

    fun userId(): String = principal().map { it.userId }.orElse(DEMO_USER_ID)

    fun requirePrincipal(): AuthPrincipal =
        principal().orElseThrow { UnauthorizedException("authentication required") }
}
