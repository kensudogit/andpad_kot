/**
 * デモ組織（org_demo）向け初期データシーダ。
 * アプリケーション起動完了（ApplicationReadyEvent）後に JDBC で
 * 組織・ユーザー・学習・建設・予算等のサンプル行を UPSERT する。
 * [DemoCatalog] のカタログ定義と [PasswordEncoder] に依存する。
 */
package jp.andpad.api.seed

import jp.andpad.api.demo.DemoCatalog
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * org_demo テナントのデモデータ投入コンポーネント（Go 版 seed.go 相当）。
 *
 * @property jdbc 生 SQL 実行用 JdbcTemplate
 * @property passwordEncoder デモユーザーパスワードの BCrypt ハッシュ化
 */
@Component
class DemoSeeder(
    private val jdbc: JdbcTemplate,
    private val passwordEncoder: PasswordEncoder,
) {

    private val log = LoggerFactory.getLogger(DemoSeeder::class.java)

    /**
     * 起動完了時にデモシードを試行する。
     * 失敗しても API 起動は継続する（本番 DB 未準備時の耐性）。
     */
    @EventListener(ApplicationReadyEvent::class)
    fun seedDemo() {
        try {
            seedDemoTx()
        } catch (ex: Exception) {
            log.error("Demo seed failed (API continues): {}", ex.message, ex)
        }
    }

    /**
     * トランザクション内で全デモデータ UPSERT を実行する。
     * テストから直接呼び出し可能（internal）。
     */
    @Transactional
    internal fun seedDemoTx() {
        ensureOrganization()
        ensureDemoUser()
        ensureLearningDemo()
        ensureConstructionDemo()
        ensureExtendedDemo()
        ensureBudgetDemo()
    }

    /**
     * デモ組織・全 SaaS モジュール有効化・利用カウンタ行を確保する。
     */
    private fun ensureOrganization() {
        jdbc.update(
            """
            INSERT INTO organizations (id, name, slug, plan_tier, subscription_status, seat_count, timezone)
            VALUES ('org_demo', 'サンプル建設株式会社', 'sample-construction', 'PRO', 'ACTIVE', 10, 'Asia/Tokyo')
            ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name
            """.trimIndent(),
        )
        // saas_modules マスタの全モジュールを org_demo で有効化
        jdbc.update(
            """
            INSERT INTO org_modules (org_id, module_code, enabled)
            SELECT 'org_demo', code, TRUE FROM saas_modules
            ON CONFLICT DO NOTHING
            """.trimIndent(),
        )
        jdbc.update(
            "INSERT INTO usage_counters (org_id) VALUES ('org_demo') ON CONFLICT (org_id) DO NOTHING",
        )
    }

    /**
     * デモログインユーザー（user_demo）と OWNER チームメンバー行を確保する。
     * パスワードは毎回 BCrypt で再ハッシュし、ログイン可能状態を維持する。
     */
    private fun ensureDemoUser() {
        val hash = passwordEncoder.encode(DEMO_PASSWORD)
        jdbc.update(
            """
            INSERT INTO users (id, email, name, password_hash)
            VALUES ('user_demo', ?, '田中 健一', ?)
            ON CONFLICT (id) DO UPDATE SET password_hash = EXCLUDED.password_hash, email = EXCLUDED.email
            """.trimIndent(),
            DEMO_EMAIL,
            hash,
        )
        jdbc.update(
            """
            INSERT INTO team_members (id, org_id, user_id, role)
            VALUES ('tm_demo', 'org_demo', 'user_demo', 'OWNER')
            ON CONFLICT (org_id, user_id) DO NOTHING
            """.trimIndent(),
        )
    }

    /**
     * [DemoCatalog] から講師・動画・学習パスを DB へ UPSERT する。
     */
    private fun ensureLearningDemo() {
        for (inst in DemoCatalog.instructors()) {
            jdbc.update(
                """
                INSERT INTO instructors (id, org_id, name, title, specialty, bio, avatar_url)
                VALUES (?, 'org_demo', ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, title = EXCLUDED.title
                """.trimIndent(),
                inst.id,
                inst.name,
                inst.title,
                inst.specialty,
                inst.bio,
                "/avatars/${inst.id}.svg",
            )
        }
        for (v in DemoCatalog.videos()) {
            jdbc.update(
                """
                INSERT INTO videos (id, org_id, instructor_id, title, description, category, procedure, skill_level,
                    duration_sec, thumbnail_url, video_url, featured, published_at)
                VALUES (?, 'org_demo', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                ON CONFLICT (id) DO UPDATE SET
                    title = EXCLUDED.title,
                    description = EXCLUDED.description,
                    thumbnail_url = EXCLUDED.thumbnail_url,
                    video_url = EXCLUDED.video_url,
                    featured = EXCLUDED.featured
                """.trimIndent(),
                v.id,
                v.instructorId,
                v.title,
                v.description,
                v.category,
                v.procedure,
                v.skillLevel,
                v.durationSec,
                v.thumbnailUrl(),
                v.embedUrl(),
                v.featured,
            )
        }
        for (path in DemoCatalog.paths()) {
            jdbc.update(
                """
                INSERT INTO learning_paths (id, org_id, title, description, category, skill_level,
                    estimated_minutes, enrolled_count, certificate_title)
                VALUES (?, 'org_demo', ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, description = EXCLUDED.description
                """.trimIndent(),
                path.id,
                path.title,
                path.description,
                path.category,
                path.skillLevel,
                path.estimatedMinutes,
                path.enrolledCount,
                path.certificate,
            )
            // パス内動画の並び順（sort_order）を 1 始まりで設定
            for (i in path.videoIds.indices) {
                jdbc.update(
                    """
                    INSERT INTO path_videos (path_id, video_id, sort_order) VALUES (?, ?, ?)
                    ON CONFLICT (path_id, video_id) DO UPDATE SET sort_order = EXCLUDED.sort_order
                    """.trimIndent(),
                    path.id,
                    path.videoIds[i],
                    i + 1,
                )
            }
        }
        ensureQuizDemo()
    }

    /**
     * クイズ・選択肢・RAG サンプル文書を投入する（相談 API デモ用）。
     */
    private fun ensureQuizDemo() {
        jdbc.update(
            """
            INSERT INTO quizzes (id, org_id, video_id, title, passing_score)
            VALUES ('quiz-v1', 'org_demo', 'v-1', '根管治療 Step1 確認テスト', 70)
            ON CONFLICT (id) DO NOTHING
            """.trimIndent(),
        )
        jdbc.update(
            """
            INSERT INTO quiz_questions (id, quiz_id, prompt, correct_index, sort_order)
            VALUES
              ('qq-v1-1', 'quiz-v1', '適切なアクセス窩形成の目的は？', 0, 1),
              ('qq-v1-2', 'quiz-v1', '根管入口の確認で最も重要なのは？', 1, 2)
            ON CONFLICT (id) DO NOTHING
            """.trimIndent(),
        )
        jdbc.update(
            """
            INSERT INTO quiz_choices (id, question_id, label, sort_order)
            VALUES
              ('qc-v1-1a', 'qq-v1-1', '根管治療器具の進入経路を確保する', 1),
              ('qc-v1-1b', 'qq-v1-1', '審美修復のみを目的とする', 2),
              ('qc-v1-1c', 'qq-v1-1', '歯肉切除を行う', 3),
              ('qc-v1-2a', 'qq-v1-2', '速度優先で拡大する', 1),
              ('qc-v1-2b', 'qq-v1-2', '根管口の位置と形状を正確に把握する', 2),
              ('qc-v1-2c', 'qq-v1-2', '必ず歯髄を残す', 3)
            ON CONFLICT (id) DO NOTHING
            """.trimIndent(),
        )
        // PostgreSQL text[] 型でタグ付き RAG 文書を 2 件投入
        jdbc.update(
            """
            INSERT INTO rag_documents (id, org_id, title, content, tags)
            VALUES
              ('rag-1', 'org_demo', '感染対策マニュアル',
               '手洗いは20秒以上、アルコール消毒はドアノブとスイッチを使用。手袋は一回のための使用を原則とする。',
               ARRAY['感染対策', '院内規程']),
              ('rag-2', 'org_demo', '予約キャンセルポリシー',
               '前日17時以降のキャンセルはキャンセル料1000円。無断キャンセルは2回で予約制限を検討する。',
               ARRAY['受付', '運営'])
            ON CONFLICT (id) DO NOTHING
            """.trimIndent(),
        )
    }

    /**
     * 建設プロジェクトとモジュール別レコード（進捗・請求・見積）のデモ行を投入する。
     */
    private fun ensureConstructionDemo() {
        jdbc.update(
            """
            INSERT INTO construction_projects (id, org_id, name, site_address, status, manager_name, start_date, end_date)
            VALUES ('prj-demo-1', 'org_demo', '渋谷オフィスビル新築工事', '東京都渋谷区道1-1-1', 'IN_PROGRESS', '山田 太郎',
                    CURRENT_DATE - 30, CURRENT_DATE + 180)
            ON CONFLICT (id) DO NOTHING
            """.trimIndent(),
        )
        jdbc.update(
            """
            INSERT INTO project_module_records (id, org_id, project_id, module_code, title, status, detail,
                amount, person_name, record_date)
            VALUES
              ('rec-demo-1', 'org_demo', 'prj-demo-1', 'CONSTRUCTION_MGMT', '基礎工程進捗確認', 'IN_PROGRESS',
               '配筋筋の配置完了、次回コンクリート打設', NULL, '山田 太郎', CURRENT_DATE),
              ('rec-bill-1', 'org_demo', 'prj-demo-1', 'BILLING', '第1回部請求', 'PAID',
               '完了出来高請求・入金済', 485000000, '山田 太郎', CURRENT_DATE - 60),
              ('rec-bill-2', 'org_demo', 'prj-demo-1', 'BILLING', '第2回部請求', 'INVOICED',
               '進捗出来高請求発行済', 495000000, '山田 太郎', CURRENT_DATE - 30),
              ('rec-inq-1', 'org_demo', 'prj-demo-1', 'INQUIRY_PROFIT', '本工事確定見積', 'WON',
               '請負金額48.5億円に対し実行予算46.8億円の粗利確保', NULL, '山田 太郎', CURRENT_DATE)
            ON CONFLICT (id) DO NOTHING
            """.trimIndent(),
        )
    }

    /**
     * 外部 API 連携・BIM モデル等の拡張モジュールデモ行を投入する。
     */
    private fun ensureExtendedDemo() {
        jdbc.update(
            """
            INSERT INTO api_integrations (id, org_id, name, provider, endpoint_url, api_key_hint, status, last_sync_at)
            VALUES ('api-demo-1', 'org_demo', 'kintone 案件連携', 'kintone',
                    'https://example.cybozu.com/k/v1/', '****7a3f', 'ACTIVE', NOW())
            ON CONFLICT (id) DO NOTHING
            """.trimIndent(),
        )
        jdbc.update(
            """
            INSERT INTO bim_models (id, org_id, project_id, title, format, viewer_url, file_size_mb, status, uploaded_by)
            VALUES ('bim-demo-1', 'org_demo', 'prj-demo-1', '本館構造BIMモデル v2', 'glTF',
                    'https://modelviewer.dev/shared-assets/models/Astronaut.glb', 128.5, 'READY', '山田 太郎')
            ON CONFLICT (id) DO NOTHING
            """.trimIndent(),
        )
    }

    /**
     * 実行予算・見積・原価エントリのデモ行を投入する（予算ダッシュボード用）。
     */
    private fun ensureBudgetDemo() {
        jdbc.update(
            """
            INSERT INTO project_budgets (id, org_id, project_id, name, budget_type, status, version_no, contract_amount, notes, approved_at)
            VALUES
              ('bud-demo-1', 'org_demo', 'prj-demo-1', '実行予算 v3', 'EXECUTION_BUDGET', 'APPROVED', 3, 4850000000,
               '本工事確定後の最終予算', NOW()),
              ('bud-demo-2', 'org_demo', 'prj-demo-1', '当初見積 v1', 'ESTIMATE', 'LOCKED', 1, 5200000000,
               '入札時点の見積', NOW())
            ON CONFLICT (id) DO NOTHING
            """.trimIndent(),
        )
        jdbc.update(
            """
            INSERT INTO budget_line_items (id, org_id, budget_id, category_code, category_name, wbs_code, description,
                estimate_amount, budget_amount, committed_amount, actual_amount, sort_order)
            VALUES
              ('bli-demo-1', 'org_demo', 'bud-demo-1', 'DIRECT', '直接工事費', 'WBS-01', '躯体工事（RC造）',
               1850000000, 1820000000, 1650000000, 980000000, 1),
              ('bli-demo-2', 'org_demo', 'bud-demo-1', 'SUBCONTRACT', '外注費', 'WBS-02', '電気・空調設備',
               980000000, 960000000, 890000000, 520000000, 2),
              ('bli-demo-3', 'org_demo', 'bud-demo-1', 'MATERIAL', '材料費', 'WBS-03', '鉄骨・コンクリート',
               720000000, 710000000, 680000000, 410000000, 3)
            ON CONFLICT (id) DO NOTHING
            """.trimIndent(),
        )
        jdbc.update(
            """
            INSERT INTO cost_entries (id, org_id, project_id, line_item_id, entry_type, vendor_name, description,
                amount, entry_date, invoice_no, recorded_by)
            VALUES
              ('cost-demo-1', 'org_demo', 'prj-demo-1', 'bli-demo-1', 'SUBCONTRACT', '株式会社××建設',
               '3階スラブコンクリート打設', 28500000, CURRENT_DATE - 3, 'INV-2026-0412', '山田 太郎'),
              ('cost-demo-2', 'org_demo', 'prj-demo-1', 'bli-demo-3', 'MATERIAL', '日本製鉄株式会社',
               'H形鋼 4階分納入', 42800000, CURRENT_DATE - 7, 'INV-2026-0398', '佐藤 花子'),
              ('cost-demo-6', 'org_demo', 'prj-demo-1', 'bli-demo-2', 'SUBCONTRACT', '△△電気工業',
               '設備工事進捗分', 385000000,
               date_trunc('month', CURRENT_DATE) - interval '3 months' + interval '20 days',
               'INV-2026-0208', '山田 太郎')
            ON CONFLICT (id) DO NOTHING
            """.trimIndent(),
        )
    }

    companion object {
        /** デモログイン用メールアドレス */
        private const val DEMO_EMAIL = "demo@sakura-dental.jp"
        /** デモログイン用平文パスワード（起動時に BCrypt 化） */
        private const val DEMO_PASSWORD = "demo1234"
    }
}
