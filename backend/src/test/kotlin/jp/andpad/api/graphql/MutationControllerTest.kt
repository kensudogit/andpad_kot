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

class MutationControllerTest : AbstractIntegrationTest() {

    @Test
    fun updateOrganizationMutation()  {
        val token = loginToken()
        mockMvc.perform(post("/graphql")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer $token")
                        .content("{\"query\":\"mutation { updateOrganization(input: { name: \\\"サンプル建設株式会社\\\", slug: \\\"sample-construction\\\", seatCount: 10, timezone: \\\"Asia/Tokyo\\\" }) { id name } }\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updateOrganization.id").value("org_demo"))
    }

}
