package jp.andpad.api.service

import jp.andpad.api.domain.AttendanceRecord
import jp.andpad.api.domain.Contract
import jp.andpad.api.domain.ContractTemplate
import jp.andpad.api.domain.CrmContact
import jp.andpad.api.domain.CrmInteraction
import jp.andpad.api.domain.DxInitiative
import jp.andpad.api.domain.LeaveRequest
import jp.andpad.api.domain.SaasModule
import jp.andpad.api.domain.SaasModuleCode
import jp.andpad.api.graphql.input.CreateContractInput
import jp.andpad.api.graphql.input.CreateCrmContactInput
import jp.andpad.api.graphql.input.CreateDxInitiativeInput
import jp.andpad.api.graphql.input.CreateLeaveRequestInput
import jp.andpad.api.repository.SaasRepository
import jp.andpad.api.security.TenantContext
import jp.andpad.api.util.Dates
import org.springframework.stereotype.Service

/**
 * SaaS 横断機能サービス（モジュール・DX・CRM・勤怠・契約）。
 *
 * **責務**: GraphQL 入力を [SaasRepository] 呼び出しに変換。
 * 勤怠打刻・休暇申請は TenantContext.userId() も使用。
 *
 * **テナント分離**: orgId / userId を TenantContext から注入。
 */
@Service
class SaasService(
    private val saasRepository: SaasRepository,
) {

    /** 組織の SaaS モジュール一覧（有効／無効含む）。 */
    fun saasModules(): List<SaasModule> {
        return saasRepository.listOrgModules(TenantContext.orgId())
    }

    /** モジュール有効／無効切替。 */
    fun setModuleEnabled(code: SaasModuleCode, enabled: Boolean): SaasModule {
        return saasRepository.setModuleEnabled(TenantContext.orgId(), code, enabled)
    }

    /** DX 施策一覧。 */
    fun dxInitiatives(): List<DxInitiative> {
        return saasRepository.listDxInitiatives(TenantContext.orgId())
    }

    /** DX 施策作成。 */
    fun createDxInitiative(input: CreateDxInitiativeInput): DxInitiative {
        return saasRepository.createDxInitiative(
            TenantContext.orgId(),
            input.title,
            input.description,
            input.status,
            input.progressPct ?: 0,
            input.ownerName,
            Dates.parseDate(input.dueDate),
        )
    }

    /** CRM 連絡先一覧。 */
    fun crmContacts(): List<CrmContact> {
        return saasRepository.listCrmContacts(TenantContext.orgId())
    }

    /** CRM 連絡先作成。 */
    fun createCrmContact(input: CreateCrmContactInput): CrmContact {
        return saasRepository.createCrmContact(
            TenantContext.orgId(),
            input.name,
            input.email,
            input.phone,
            input.company,
            input.stage,
            input.notes,
        )
    }

    /** 連絡先のインタラクション履歴。 */
    fun crmInteractions(contactId: String): List<CrmInteraction> {
        return saasRepository.listCrmInteractions(TenantContext.orgId(), contactId)
    }

    /** CRM インタラクション記録。 */
    fun createCrmInteraction(contactId: String, kind: String, summary: String): CrmInteraction {
        return saasRepository.createCrmInteraction(TenantContext.orgId(), contactId, kind, summary)
    }

    /** 勤怠記録一覧（直近 30 件）。 */
    fun attendanceRecords(): List<AttendanceRecord> {
        return saasRepository.listAttendanceRecords(TenantContext.orgId())
    }

    /** 現在ユーザーの出勤打刻。 */
    fun clockIn(note: String?): AttendanceRecord {
        return saasRepository.clockIn(TenantContext.orgId(), TenantContext.userId(), note ?: "")
    }

    /**
     * 現在ユーザーの退勤打刻。
     *
     * @throws EmptyResultDataAccessException 未退勤レコード不存在
     */
    fun clockOut(): AttendanceRecord {
        return saasRepository.clockOut(TenantContext.orgId(), TenantContext.userId())
    }

    /** 休暇申請一覧。 */
    fun leaveRequests(): List<LeaveRequest> {
        return saasRepository.listLeaveRequests(TenantContext.orgId())
    }

    /**
     * 休暇申請作成（現在ユーザー）。
     *
     * @throws IllegalArgumentException 日付パース失敗
     */
    fun createLeaveRequest(input: CreateLeaveRequestInput): LeaveRequest {
        val start = Dates.parseDate(input.startDate)
            ?: throw IllegalArgumentException("invalid startDate")
        val end = Dates.parseDate(input.endDate)
            ?: throw IllegalArgumentException("invalid endDate")
        return saasRepository.createLeaveRequest(
            TenantContext.orgId(),
            TenantContext.userId(),
            start,
            end,
            input.reason,
        )
    }

    /** 休暇申請承認。 */
    fun approveLeaveRequest(id: String): LeaveRequest {
        return saasRepository.approveLeaveRequest(TenantContext.orgId(), id)
    }

    /** 契約テンプレート一覧。 */
    fun contractTemplates(): List<ContractTemplate> {
        return saasRepository.listContractTemplates(TenantContext.orgId())
    }

    /** 契約テンプレート作成。 */
    fun createContractTemplate(name: String, body: String): ContractTemplate {
        return saasRepository.createContractTemplate(TenantContext.orgId(), name, body)
    }

    /** 契約書一覧。 */
    fun contracts(): List<Contract> {
        return saasRepository.listContracts(TenantContext.orgId())
    }

    /** 契約書作成（テンプレート本文コピー可）。 */
    fun createContract(input: CreateContractInput): Contract {
        return saasRepository.createContract(
            TenantContext.orgId(),
            input.templateId,
            input.title,
            input.partyName,
            input.partyEmail,
            input.body,
        )
    }

    /** 契約書署名（status → SIGNED）。 */
    fun signContract(id: String): Contract {
        return saasRepository.signContract(TenantContext.orgId(), id)
    }
}
