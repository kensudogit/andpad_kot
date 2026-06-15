package jp.andpad.api.demo

import jp.andpad.api.AbstractIntegrationTest
import jp.andpad.api.demo.DemoCatalog.CatalogVideo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class DemoCatalogTest : AbstractIntegrationTest() {

    @Test
    fun catalogHasVideosAndPaths() {
        assertThat(DemoCatalog.videos()).hasSize(10)
        assertThat(DemoCatalog.paths()).hasSize(6)
        val v = DemoCatalog.videos().first()
        assertThat(v.thumbnailUrl()).contains("youtube.com")
    }

}
