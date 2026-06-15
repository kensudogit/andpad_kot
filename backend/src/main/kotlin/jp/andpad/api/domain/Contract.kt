package jp.andpad.api.domain

data class Contract(
    val id: String,
    val templateId: String,
    val title: String,
    val partyName: String,
    val partyEmail: String,
    val body: String,
    val status: String,
    val createdAt: String,
    val signedAt: String,
)
