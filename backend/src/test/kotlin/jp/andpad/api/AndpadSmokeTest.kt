package jp.andpad.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AndpadSmokeTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var authRepository: jp.andpad.api.repository.AuthRepository

    @Test
    fun healthReturnsOk()  {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok")
                        .value(true))
    }

    @Test
    fun loginAndQueryConstructionProjects()  {
        val token = loginToken()
        mockMvc.perform(
                        post("/graphql")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer $token")
                                .content("""
                                        {"query":"{ constructionProjects { id name status } }"}
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                                "$.data.constructionProjects[0].id")
                        .value("prj-demo-1"))
    }

    @Test
    fun loginAndQueryLearningCatalog()  {
        val token = loginToken()
        mockMvc.perform(
                        post("/graphql")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer $token")
                                .content("""
                                        {"query":"{ dashboard { videosTotal learningPathsTotal } featuredVideos { id featured } learningPaths { id videoIds } }"}
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                                "$.data.dashboard.videosTotal")
                        .value(10))
                .andExpect(jsonPath(
                                "$.data.dashboard.learningPathsTotal")
                        .value(6))
                .andExpect(jsonPath(
                                "$.data.featuredVideos[0].id")
                        .value("v-1"))
    }

    @Test
    fun loginAndQueryRagDocuments()  {
        val token = loginToken()
        mockMvc.perform(
                        post("/graphql")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer $token")
                                .content("""{"query":"{ ragDocuments { id title } ragSearch(query: \"RAG\", limit: 3) { title score } }"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                                "$.data.ragDocuments[0].id")
                        .value("rag-1"))
    }

    @Test
    fun authRepositoryFindsDemoUser() {
        assertThat(authRepository.findUserByEmail("demo@sakura-dental.jp")).isPresent()
    }
}
