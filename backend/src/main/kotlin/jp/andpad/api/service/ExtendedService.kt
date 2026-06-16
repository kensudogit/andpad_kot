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

/**
 * 拡張機能（分析・API 連携・BIM）サービス。
 *
 * **責務**: ANDPAD 風分析、外部 API 連携、BIM モデル管理、分析インサイト生成。
 * orgId は [TenantContext] から注入。
 *
 * **テナント分離**: 全リポジトリ呼び出しで TenantContext.orgId() を使用。
 */
@Service
class ExtendedService(
    private val extendedRepository: ExtendedRepository,
) {

    /** 指定期間の組織横断分析ダッシュボード。 */
    fun andpadAnalytics(periodDays: Int): AndpadAnalyticsDashboard {
        return extendedRepository.andpadAnalytics(TenantContext.orgId(), periodDays)
    }

    /** API 連携設定一覧。 */
    fun listApiIntegrations(): List<ApiIntegration> {
        return extendedRepository.listApiIntegrations(TenantContext.orgId())
    }

    /** API 連携設定作成。 */
    fun createApiIntegration(input: CreateApiIntegrationInput): ApiIntegration {
        return extendedRepository.createApiIntegration(TenantContext.orgId(), input)
    }

    /** API 連携の同期日時更新。 */
    fun syncApiIntegration(id: String): ApiIntegration {
        return extendedRepository.syncApiIntegration(TenantContext.orgId(), id)
    }

    /** プロジェクト別 BIM モデル一覧。 */
    fun listBimModels(projectId: String): List<BimModel> {
        return extendedRepository.listBimModels(TenantContext.orgId(), projectId)
    }

    /**
     * BIM モデル 1 件取得。
     *
     * @throws IllegalArgumentException 不存在時
     */
    fun getBimModel(id: String): BimModel {
        return extendedRepository.getBimModel(TenantContext.orgId(), id)
            ?: throw IllegalArgumentException("bim model not found")
    }

    /** BIM モデル作成。 */
    fun createBimModel(input: CreateBimModelInput): BimModel {
        return extendedRepository.createBimModel(TenantContext.orgId(), input)
    }

    /** 分析ダッシュボードから定型インサイト文を生成（OpenAI 非使用フォールバック）。 */
    fun generateAnalyticsInsight(periodDays: Int): AnalyticsInsight {
        val dash = andpadAnalytics(periodDays)
        return fallbackConstructionInsight(dash)
    }

    /** ダッシュボード KPI から日本語サマリ・推奨アクションを組み立て。 */
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
