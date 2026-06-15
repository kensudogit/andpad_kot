package jp.andpad.api.auth.dto

data class RegisterRequest(val clinicName: String, val slug: String, val ownerName: String, val email: String, val password: String)
