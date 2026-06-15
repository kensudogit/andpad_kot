package jp.andpad.api.domain

data class TeamMember(val id: String, val user: User, val role: MemberRole, val joinedAt: String, val lastActiveAt: String)
