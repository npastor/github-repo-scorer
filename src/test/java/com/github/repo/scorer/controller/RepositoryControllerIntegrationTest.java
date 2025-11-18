package com.github.repo.scorer.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.repo.scorer.client.GithubFeignClient;
import com.github.repo.scorer.client.Repository;
import com.github.repo.scorer.model.SearchRepositoriesResponse;
import com.github.repo.scorer.service.RepositorySearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RepositoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GithubFeignClient githubFeignClient;

    private RepositorySearchService searchService;

    @Test
    void getRepositories_returnsDataWithScoreAndSortedByHighestScore() throws Exception {
        Repository repository_1 = new Repository(1, "repo1", "repo1 description", "2020-11-18T12:00:00Z", "2025-11-18T12:00:00Z", 100, 50000, "java");
        Repository repository_2 = new Repository(2, "repo2", "repo2 description", "2019-11-18T12:00:00Z", "2025-11-18T12:00:00Z", 200, 400, "java");
        SearchRepositoriesResponse mockResponse = new SearchRepositoriesResponse(2, List.of(repository_1, repository_2));
        when(githubFeignClient.searchRepositories(anyString(), anyInt(), anyInt()))
                .thenReturn(mockResponse);

        var result = mockMvc.perform(get("/api/v1/repositories")
                        .param("language", "Java")
                        .param("created_after", "2025-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_count").value(2))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.page_size").value(100))
                .andExpect(jsonPath("$.repositories[0].name").value("repo1"))
                .andExpect(jsonPath("$.repositories[1].name").value("repo2"))
                .andExpect(jsonPath("$.repositories[0].description").value("repo1 description"))
                .andExpect(jsonPath("$.repositories[1].description").value("repo2 description"))
                .andExpect(jsonPath("$.repositories[0].score").isNotEmpty())
                .andExpect(jsonPath("$.repositories[1].score").isNotEmpty())
                .andExpect(jsonPath("$.repositories[0].created_at").value("2020-11-18T12:00:00Z"))
                .andExpect(jsonPath("$.repositories[1].created_at").value("2019-11-18T12:00:00Z"))
                .andExpect(jsonPath("$.repositories[0].language").value("java"))
                .andExpect(jsonPath("$.repositories[1].language").value("java"))
                .andReturn().getResponse().getContentAsString();


        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(result);
        JsonNode repositoriesNode = rootNode.path("repositories");

        double score0 = repositoriesNode.get(0).path("score").asDouble();
        double score1 = repositoriesNode.get(1).path("score").asDouble();

        assertTrue(score0 >= score1, "Expected first repository to have score >= second repository");
    }

    @Test
    void getRepositories_noResultsFound() throws Exception {
        SearchRepositoriesResponse mockResponse = new SearchRepositoriesResponse(0, Collections.emptyList());
        when(githubFeignClient.searchRepositories(anyString(), anyInt(), anyInt()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/repositories")
                        .param("language", "Java")
                        .param("created_after", "2025-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_count").value(0))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.page_size").value(100))
                .andExpect(jsonPath("$.repositories").isEmpty());
    }

}
