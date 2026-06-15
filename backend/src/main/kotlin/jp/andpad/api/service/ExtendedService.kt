package jp.andpad.api.service

import jp.andpad.api.domain.ExtendedTypes.AndpadAnalyticsDashboard
import jp.andpad.api.domain.ExtendedTypes.ApiIntegration
import jp.andpad.api.domain.ExtendedTypes.BimModel
import jp.andpad.api.domain.LearningTypes.AnalyticsInsight
import jp.andpad.api.graphql.input.LearningInputs.CreateApiIntegrationInput
import jp.andpad.api.graphql.input.LearningInputs.CreateBimModelInput
import jp.andpad.api.repository.ExtendedRepository
import jp.andpad.api.security.TenantContext
import jp.andpad.api.util.Dates
import org.springframework.stereotype.Service

@Service
class ExtendedService(
    private val extendedRepository: ExtendedRepository,
) {

    fun andpadAnalytics(periodDays: Int): AndpadAnalyticsDashboard {
        return extendedRepository.andpadAnalytics(TenantContext.orgId(), periodDays)
    }

    fun listApiIntegrations(): List<ApiIntegration> {
        return extendedRepository.listApiIntegrations(TenantContext.orgId())
    }

    fun createApiIntegration(input: CreateApiIntegrationInput): ApiIntegration {
        return extendedRepository.createApiIntegration(TenantContext.orgId(), input)
    }

    fun syncApiIntegration(id: String): ApiIntegration {
        return extendedRepository.syncApiIntegration(TenantContext.orgId(), id)
    }

    fun listBimModels(projectId: String): List<BimModel> {
        return extendedRepository.listBimModels(TenantContext.orgId(), projectId)
    }

    fun getBimModel(id: String): BimModel {
        return extendedRepository.getBimModel(TenantContext.orgId(), id)
            ?: throw IllegalArgumentException("bim model not found")
    }

    fun createBimModel(input: CreateBimModelInput): BimModel {
        return extendedRepository.createBimModel(TenantContext.orgId(), input)
    }

    fun generateAnalyticsInsight(periodDays: Int): AnalyticsInsight {
        val dash = andpadAnalytics(periodDays)
        return fallbackConstructionInsight(dash)
    }

    private fun fallbackConstructionInsight(d: AndpadAnalyticsDashboard): AnalyticsInsight {
        val summary = String.format(
            "過去%d日間: 進行中案件%d件、健全性%.0f点。請求¥%.0f、実行予算¥%.0f、期間原価¥%.0f、予算差異率%.1f%%。",
            d.periodDays,
            d.activeProjects,
            d.projectHealthScore,
            d.billingTotal,
            d.budgetTotal,
            d.costTotal,
            d.budgetVariancePct,
        )
        return AnalyticsInsight(
            summary,
            listOf("予算・原価・請求データが案件横断で可視化されています"),
            listOf("原価進捗が予算に対して高い案件は完工予想の再算定が必要です"),
            listOf("月次原価レポートで費目別差異を確認", "請求と原価のバランスを四半期ごとにレビュー"),
            Dates.format(Dates.now()) ?: "",
        )
    }
}
