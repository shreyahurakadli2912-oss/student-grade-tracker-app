package com.studentgradetracker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SpaRoutingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Verify root URL '/' forwards to index.html for React SPA")
    void testRootUrlForwardsToSpa() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    @DisplayName("Verify client-side routes forward to index.html for direct access and page refreshes")
    void testClientSideRoutesForwardToSpa() throws Exception {
        String[] clientRoutes = {"/students", "/dashboard", "/import", "/reports", "/analytics", "/gradescale", "/settings"};

        for (String route : clientRoutes) {
            mockMvc.perform(get(route))
                    .andExpect(status().isOk())
                    .andExpect(forwardedUrl("/index.html"));
        }
    }

    @Test
    @DisplayName("Verify health check endpoint works at both /api/health and /health")
    void testHealthEndpoints() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.service", is("Student Grade Management System")));

        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    @DisplayName("Verify API 404 errors are NOT intercepted by SPA forwarding")
    void testApi404NotInterceptedBySpa() throws Exception {
        mockMvc.perform(get("/api/nonexistent-endpoint"))
                .andExpect(status().isNotFound())
                .andExpect(forwardedUrl(null));
    }
}
