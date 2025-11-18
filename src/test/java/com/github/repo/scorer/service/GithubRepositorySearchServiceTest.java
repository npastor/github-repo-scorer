package com.github.repo.scorer.service;

import com.github.repo.scorer.client.GithubFeignClient;
import com.github.repo.scorer.client.Repository;
import com.github.repo.scorer.exception.InternalServerErrorException;
import com.github.repo.scorer.exception.UnprocessableEntityException;
import com.github.repo.scorer.model.PageRequest;
import com.github.repo.scorer.model.SearchRepositoriesResponse;
import com.github.repo.scorer.model.SearchRepositoryQuery;
import com.github.repo.scorer.model.SearchRepositoryRequest;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GithubRepositorySearchServiceTest {

    private GithubFeignClient githubClient;
    private RepositoryScorer repositoryScorer;
    private RepositorySearchService service;

    @BeforeEach
    void setup() {
        githubClient = mock(GithubFeignClient.class);
        repositoryScorer = mock(RepositoryScorer.class);
        service = new GithubRepositorySearchService(githubClient, repositoryScorer);
    }

    @Test
    void getScoredRepositories_returnsScoredRepositoriesSortedByScoreASC() {
        var repoWithLessStars = new Repository(1, "repo1", "desc1", "2023-01-27T02:25:38Z", "2023-01-27T02:25:38Z", 500,
                1000, "Java");
        var repoWithMoreStars = new Repository(2, "repo2", "desc2", "2022-01-27T02:25:38Z", "2023-01-27T02:25:38Z", 500,
                1001, "Java");
        var searchResponse = new SearchRepositoriesResponse(2, List.of(repoWithLessStars, repoWithMoreStars));
        when(githubClient.searchRepositories(anyString(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(searchResponse);
        var request = new SearchRepositoryRequest(
                new SearchRepositoryQuery("Java", "2020-01-01"),
                new PageRequest(1, 10)
        );

        var response = service.searchAndScore(request);

        assertEquals(2, response.total_count());
        assertEquals(10, response.page());
        assertEquals(1, response.page_size());
        assertEquals(1, response.repositories().get(0).id());
        assertEquals("repo1", response.repositories().get(0).name());
        assertEquals("repo2", response.repositories().get(1).name());
        assertEquals("desc1", response.repositories().get(0).description());
        assertEquals("desc2", response.repositories().get(1).description());
        assertEquals("2023-01-27T02:25:38Z", response.repositories().get(0).created_at());
        assertEquals("2022-01-27T02:25:38Z", response.repositories().get(1).created_at());
        assertEquals(0.0, response.repositories().get(0).score());
        assertEquals(0.0, response.repositories().get(1).score());
        assertEquals("Java", response.repositories().get(0).language());
        assertEquals("Java", response.repositories().get(1).language());
    }

    @Test
    void getScoredRepositories_returnsEmptyResponseIfGitHubSearchResponseIsNull() {
        when(githubClient.searchRepositories(anyString(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(null);
        var request = new SearchRepositoryRequest(
                new SearchRepositoryQuery("Java", "2020-01-01"),
                new PageRequest(1, 10)
        );

        var response = service.searchAndScore(request);

        assertEquals(0, response.total_count());
        assertEquals(10, response.page());
        assertEquals(1, response.page_size());
        assertEquals(0, response.repositories().size());
    }

    @Test
    void getScoredRepositories_returnsEmptyResponseIfGitHubSearchResponseRepositoriesIsNull() {
        var searchResponse = new SearchRepositoriesResponse(0, null);
        when(githubClient.searchRepositories(anyString(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(searchResponse);
        var request = new SearchRepositoryRequest(
                new SearchRepositoryQuery("Java", "2020-01-01"),
                new PageRequest(1, 10)
        );

        var response = service.searchAndScore(request);

        assertEquals(0, response.total_count());
        assertEquals(10, response.page());
        assertEquals(1, response.page_size());
        assertEquals(0, response.repositories().size());
    }

    @Test
    void getScoredRepositories_callsGithubClient_WithCorrectArguments() {
        var repo = new Repository(1, "repo", "desc", "2023-01-27T02:25:38Z", "2023-01-27T02:25:38Z", 5,
                2000, "Java");
        var response = new SearchRepositoriesResponse(1, List.of(repo));

        when(githubClient.searchRepositories(anyString(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(response);
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> pageCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> pageSizeCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> sortByCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> sortOrderCaptor = ArgumentCaptor.forClass(String.class);
        var request = new SearchRepositoryRequest(
                new SearchRepositoryQuery("Java", "2020-01-01"),
                new PageRequest(1, 10)
        );

        service.searchAndScore(request);

        verify(githubClient).searchRepositories(queryCaptor.capture(), pageCaptor.capture(), pageSizeCaptor.capture(), sortByCaptor.capture(), sortOrderCaptor.capture());
        assertEquals("language:Java created:>2020-01-01 archived:false mirror:false", queryCaptor.getValue());
        assertEquals(1, pageCaptor.getValue());
        assertEquals(10, pageSizeCaptor.getValue());
    }


    @Test
    void testFeignException422ThrowsUnprocessableEntity() {
        var request = new SearchRepositoryRequest(
                new SearchRepositoryQuery("Java", "2020-01-01"),
                new PageRequest(1, 10)
        );
        Request feignRequest = Request.create(
                Request.HttpMethod.GET,
                "/repositories",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                null
        );
        when(githubClient.searchRepositories(anyString(), anyInt(), anyInt(), anyString(), anyString()))
                .thenThrow(new FeignException.UnprocessableEntity(
                        "422 Unprocessable Entity",
                        feignRequest,
                        null,
                        Collections.emptyMap()
                ));

        assertThrows(UnprocessableEntityException.class, () -> service.searchAndScore(request));
    }

    @Test
    void testFeignExceptionOtherThrowsInternalServerError() {
        var request = new SearchRepositoryRequest(
                new SearchRepositoryQuery("Java", "2020-01-01"),
                new PageRequest(1, 10)
        );
        Request feignRequest = Request.create(
                Request.HttpMethod.GET,
                "/repositories",
                Collections.emptyMap(),
                null,
                StandardCharsets.UTF_8,
                null
        );
        when(githubClient.searchRepositories(anyString(), anyInt(), anyInt(), anyString(), anyString()))
                .thenThrow(new FeignException.InternalServerError(
                        "503 Internal Error",
                        feignRequest,
                        null,
                        Collections.emptyMap()
                ));

        assertThrows(InternalServerErrorException.class, () -> service.searchAndScore(request));
    }
}
