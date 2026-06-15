package jp.andpad.api.graphql

import jp.andpad.api.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class QueryControllerTest : AbstractIntegrationTest() {

    @Test
    fun currentSessionWithoutAuthReturnsNull()  {
        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"{ currentSession { user { email } } }\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentSession").isEmpty())
    }

    @Test
    fun consultThreadsWithoutAuthReturnsUnauthorized()  {
        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"{ consultThreads { id } }\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors[0].message")
                        .value("authentication required"))
                .andExpect(jsonPath("$.errors[0].extensions.classification")
                        .value("UNAUTHORIZED"))
    }

    @Test
    fun constructionProjectsQuery()  {
        val token = loginToken()
        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer $token")
                        .content("{\"query\":\"{ constructionProjects { id } }\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.constructionProjects[0].id").value("prj-demo-1"))
    }

}
