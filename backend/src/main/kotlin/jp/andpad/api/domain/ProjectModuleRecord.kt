package jp.andpad.api.domain

data class ProjectModuleRecord(
    val id: String,
    val projectId: String,
    val projectName: String,
    val moduleCode: SaasModuleCode,
    val title: String,
    val status: String,
    val detail: String,
    val amount: Double?,
    val personName: String,
    val recordDate: String,
    val createdAt: String,
)
