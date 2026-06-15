package jp.andpad.api.service

import jp.andpad.api.auth.dto.AuthResponse
import jp.andpad.api.domain.Session
import jp.andpad.api.repository.AuthRepository
import jp.andpad.api.repository.AuthRepository.LoginResult
import jp.andpad.api.repository.AuthRepository.RegisterInput
import jp.andpad.api.security.AuthPrincipal
import jp.andpad.api.security.JwtService
import jp.andpad.api.security.TenantContext
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authRepository: AuthRepository,
    private val jwtService: JwtService,
    private val organizationService: OrganizationService,
) {

    fun login(email: String, password: String): AuthResponse {
        val result = authRepository.login(email, password)
            .orElseThrow { IllegalArgumentException("invalid credentials") }
        return toAuthResponse(result)
    }

    fun register(input: RegisterInput): AuthResponse {
        val result = authRepository.register(input)
        return toAuthResponse(result)
    }

    fun currentSession(): Session? {
        val principal = TenantContext.principal()
        if (principal.isEmpty) {
            return null
        }
        val p = principal.get()
        val result = authRepository.sessionByUser(p.userId, p.orgId)
        return result.map(this::toSession).orElse(null)
    }

    private fun toAuthResponse(result: LoginResult): AuthResponse {
        val session = toSession(result)
        val principal = AuthPrincipal(
            result.user.id,
            result.organization.id,
            result.role.name,
            result.user.email,
            result.user.name,
        )
        val token = jwtService.issueToken(principal)
        return AuthResponse(token, session)
    }

    private fun toSession(result: LoginResult): Session {
        val org = organizationService.enrichOrganization(result.organization)
        return Session(result.user, org, result.role)
    }
}
