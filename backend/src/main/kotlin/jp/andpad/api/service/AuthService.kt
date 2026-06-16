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

/**
 * 認証・セッション管理サービス。
 *
 * **責務**: ログイン／登録のオーケストレーション、JWT 発行、現在セッション復元。
 * DB アクセスは [AuthRepository] に委譲し、組織情報の enrich は [OrganizationService] 経由。
 *
 * **テナント分離**: リポジトリ呼び出し時に orgId を渡す。currentSession は [TenantContext] から取得。
 */
@Service
class AuthService(
    private val authRepository: AuthRepository,
    private val jwtService: JwtService,
    private val organizationService: OrganizationService,
) {

    /**
     * メール＋パスワードでログインし JWT とセッションを返す。
     *
     * @throws IllegalArgumentException 認証失敗時
     */
    fun login(email: String, password: String): AuthResponse {
        val result = authRepository.login(email, password)
            .orElseThrow { IllegalArgumentException("invalid credentials") }
        return toAuthResponse(result)
    }

    /** 新規組織登録後、JWT とセッションを返す。 */
    fun register(input: RegisterInput): AuthResponse {
        val result = authRepository.register(input)
        return toAuthResponse(result)
    }

    /**
     * 認証済みユーザーの現在セッションを返す。
     *
     * @return 未認証または所属不一致時 null
     */
    fun currentSession(): Session? {
        val principal = TenantContext.principal()
        if (principal.isEmpty) {
            return null
        }
        val p = principal.get()
        val result = authRepository.sessionByUser(p.userId, p.orgId)
        return result.map(this::toSession).orElse(null)
    }

    /** [LoginResult] から JWT 付き [AuthResponse] を組み立て。 */
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

    /** [LoginResult] をモジュール付き [Session] に変換。 */
    private fun toSession(result: LoginResult): Session {
        val org = organizationService.enrichOrganization(result.organization)
        return Session(result.user, org, result.role)
    }
}
