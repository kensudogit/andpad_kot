package jp.andpad.api.security

data class AuthPrincipal(
    val userId: String,
    val orgId: String,
    val role: String,
    val email: String,
    val name: String,
)
