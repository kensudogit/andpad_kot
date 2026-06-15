package jp.andpad.api.auth.dto

import jp.andpad.api.domain.Session

data class AuthResponse(val token: String, val session: Session)
