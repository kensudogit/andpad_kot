package jp.andpad.api.domain

data class CrmContact(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val company: String,
    val stage: String,
    val notes: String,
    val createdAt: String,
)
