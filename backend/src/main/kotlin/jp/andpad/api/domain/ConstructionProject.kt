package jp.andpad.api.domain

data class ConstructionProject(
    val id: String,
    val name: String,
    val siteAddress: String,
    val status: ConstructionProjectStatus,
    val managerName: String,
    val startDate: String,
    val endDate: String,
    val recordCount: Int,
    val createdAt: String,
)
