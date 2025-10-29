package com.war.game.war_backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.war.game.war_backend.config.BaseTestConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(BaseTestConfiguration.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthCheckShouldReturnOkWhenDatabaseIsUp() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("ok"))
               .andExpect(jsonPath("$.uptime").isNumber())
               .andExpect(jsonPath("$.timestamp").isString())
               .andExpect(jsonPath("$.duration").isNumber());
    }
}
