package jp.andpad.api.util

import jp.andpad.api.domain.ExtendedTypes.RagDocument
import jp.andpad.api.domain.ExtendedTypes.RagSearchHit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RagHelperTest {

    @Test
    fun localSearchFindsDocument() {
        val docs = listOf(RagDocument("d1", "感染管理", "手指衛生の重要性", listOf("tag"), "2024-01-01"))
        val hits = RagHelper.localSearch(docs, "感染", 3)
        assertThat(hits).hasSize(1)
        assertThat(RagHelper.formatHitsAsAnswer(hits)).contains("感染管理")
    }

}
