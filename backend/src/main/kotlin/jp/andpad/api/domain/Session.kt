package jp.andpad.api.domain

data class Session(val user: User, val organization: Organization, val role: MemberRole)
