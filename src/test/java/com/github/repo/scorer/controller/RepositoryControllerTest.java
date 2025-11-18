package com.github.repo.scorer.controller;

import com.github.repo.scorer.model.ScoredRepositoriesResponse;
import com.github.repo.scorer.model.ScoredRepository;
import com.github.repo.scorer.model.SearchRepositoryRequest;
import com.github.repo.scorer.service.RepositorySearchService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RepositoryController.class)
class RepositoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RepositorySearchService repositorySearchService;

    @Test
    void getRepositories_withDefaultPagination_returnsOk() throws Exception {
        ScoredRepository repository_1 = new ScoredRepository(1, "repo1", "repo1 description", 0.4, "java", "2012-12-12");
        ScoredRepository repository_2 = new ScoredRepository(2, "repo2", "repo2 description", 0.3, "java", "2012-12-12");
        ScoredRepositoriesResponse mockResponse = new ScoredRepositoriesResponse(2, 1, 100, List.of(repository_1, repository_2));
        when(repositorySearchService.searchAndScore(any(SearchRepositoryRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/repositories")
                        .param("language", "Java")
                        .param("created_after", "2025-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_count").value(2))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.page_size").value(100))
                .andExpect(jsonPath("$.repositories[0].name").value("repo1"))
                .andExpect(jsonPath("$.repositories[1].name").value("repo2"));

        ArgumentCaptor<SearchRepositoryRequest> captor = ArgumentCaptor.forClass(SearchRepositoryRequest.class);
        verify(repositorySearchService).searchAndScore(captor.capture());
        SearchRepositoryRequest capturedRequest = captor.getValue();

        assertEquals(1, capturedRequest.pageRequest().page());
        assertEquals(100, capturedRequest.pageRequest().pageSize());
    }

    @Test
    void getRepositories_withUserConfiguredPagination_returnsOk() throws Exception {
        ScoredRepository repository_1 = new ScoredRepository(1, "repo1", "repo1 description", 0.4, "java", "2012-12-12");
        ScoredRepository repository_2 = new ScoredRepository(2, "repo2", "repo2 description", 0.3, "java", "2012-12-12");
        ScoredRepositoriesResponse mockResponse = new ScoredRepositoriesResponse(2, 2, 50, List.of(repository_1, repository_2));
        when(repositorySearchService.searchAndScore(any(SearchRepositoryRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/repositories")
                        .param("language", "Java")
                        .param("created_after", "2025-01-01")
                        .param("page", "2")
                        .param("page_size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_count").value(2))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.page_size").value(50))
                .andExpect(jsonPath("$.repositories[0].name").value("repo1"))
                .andExpect(jsonPath("$.repositories[1].name").value("repo2"));

        ArgumentCaptor<SearchRepositoryRequest> captor = ArgumentCaptor.forClass(SearchRepositoryRequest.class);
        verify(repositorySearchService).searchAndScore(captor.capture());
        SearchRepositoryRequest capturedRequest = captor.getValue();

        assertEquals(2, capturedRequest.pageRequest().page());
        assertEquals(50, capturedRequest.pageRequest().pageSize());
    }


    @Test
    void getRepositories_shouldFail_ifCreatedAfterIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/repositories")
                        .param("language", "Java"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message")
                        .value("Missing required request parameter: 'created_after'"));
    }

    @Test
    void getRepositories_shouldFail_ifCreatedAfterFormatIsIncorrect() throws Exception {
        mockMvc.perform(get("/api/v1/repositories")
                        .param("language", "Java")
                        .param("created_after", "01-01-2010"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message")
                        .value("Parameter 'created_after' must be of type 'LocalDate'"));
    }

    @Test
    void getRepositories_shouldFail_ifLanguageIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/repositories")
                        .param("created_after", "2020-01-01"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.message")
                        .value("Missing required request parameter: 'language'"));
    }

    @Test
    void getRepositories_shouldFail_PageSizeExceedsMaxValue() throws Exception {
        mockMvc.perform(get("/api/v1/repositories")
                        .param("language", "Java")
                        .param("created_after", "2020-01-01")
                        .param("page_size", "101"))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.details[0]")
                        .value("getRepositories.pageSize: must be less than or equal to 100"));
    }


}