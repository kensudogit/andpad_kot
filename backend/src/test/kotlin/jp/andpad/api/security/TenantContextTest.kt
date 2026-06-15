package jp.andpad.api.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class TenantContextTest {

    @AfterEach
    fun clearContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun returnsDemoIdsWhenUnauthenticated() {
        assertThat(TenantContext.orgId()).isEqualTo(TenantContext.DEMO_ORG_ID)
        assertThat(TenantContext.userId()).isEqualTo(TenantContext.DEMO_USER_ID)
    }

    @Test
    fun readsAuthenticatedPrincipal() {
        val principal = AuthPrincipal("u9", "org9", "OWNER", "a@b.jp", "Name")
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken(principal, null))
        assertThat(TenantContext.orgId()).isEqualTo("org9")
        assertThat(TenantContext.userId()).isEqualTo("u9")
    }

}
