package jp.andpad.api.domain

data class CrmInteraction(val id: String, val contactId: String, val kind: String, val summary: String, val occurredAt: String)
