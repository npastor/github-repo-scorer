package com.github.repo.scorer.service;

import com.github.repo.scorer.client.GithubFeignClient;
import com.github.repo.scorer.client.Repository;
import com.github.repo.scorer.exception.InternalServerErrorException;
import com.github.repo.scorer.exception.UnprocessableEntityException;
import com.github.repo.scorer.model.*;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;

@Service
public class GithubRepositorySearchService implements RepositorySearchService {
    private static final Instant MIN_GITHUB_PUSHED_AT = Instant.parse("2008-04-01T00:00:00Z");
    private static final Logger log = LoggerFactory.getLogger(GithubRepositorySearchService.class);
    private final GithubFeignClient githubClient;
    private final RepositoryScorer repositoryScorer;

    public GithubRepositorySearchService(GithubFeignClient githubClient, RepositoryScorer repositoryScorer) {
        this.githubClient = githubClient;
        this.repositoryScorer = repositoryScorer;
    }


    @Override
    public ScoredRepositoriesResponse searchAndScore(SearchRepositoryRequest request) {
        String query = buildSearchQuery(request.query());
        int pageSize = request.pageRequest().pageSize();
        int page = request.pageRequest().page();

        SearchRepositoriesResponse response = searchRepositories(query, page, pageSize);

        return mapToScoredResponse(response, page, pageSize);
    }

    /*
     * As we are rating repositories based on popularity, we could narrow the search to only
     * score repositories that are not archived and are not a mirror.
     * */
    private String buildSearchQuery(SearchRepositoryQuery query) {
        return new SearchQueryBuilder.Builder(query.language(), query.createdAfter())
                .archived(false)
                .mirror(false)
                .build()
                .buildQueryString();
    }

    private SearchRepositoriesResponse searchRepositories(String query, int page, int pageSize) {
        SearchRepositoriesResponse response = null;
        try {
            log.info("Starting GitHub repository search with query: {}", query);
            response = githubClient.searchRepositories(query, pageSize, page);
            if (response != null) log.info("Found results, total count: {}", response.total_count());
        } catch (FeignException e) {
            mapException(e);
        }
        return response;
    }


    private ScoredRepositoriesResponse mapToScoredResponse(SearchRepositoriesResponse response, int page, int pageSize) {
        if (response == null || CollectionUtils.isEmpty(response.items())) {
            return new ScoredRepositoriesResponse(0, page, pageSize, Collections.emptyList());
        }

        var repositories = response.items().stream()
                .filter(repository -> !repository.pushed_at().isBlank())
                .map(this::toScoredRepository)
                .sorted(Comparator.comparingDouble(ScoredRepository::score).reversed())
                .toList();
        return new ScoredRepositoriesResponse(response.total_count(), page, pageSize, repositories);

    }

    private ScoredRepository toScoredRepository(Repository repository) {
        Instant updatedAtInstant = getUpdatedAtInstant(repository);
        long daysSinceLastUpdated = ChronoUnit.DAYS.between(updatedAtInstant, Instant.now());
        double score = repositoryScorer.calculateScore(
                repository.stargazers_count(),
                repository.forks_count(),
                daysSinceLastUpdated
        );
        return new ScoredRepository(
                repository.id(),
                repository.name(),
                repository.description(),
                score,
                repository.language(),
                repository.created_at()
        );
    }

    private void mapException(FeignException exception) {
        if (exception.status() == 422) {
            throw new UnprocessableEntityException("Search criteria cannot be applied or API has been spammed.");
        }
        log.error("Something went wrong while searching for repositories: {}", exception.getMessage());
        throw new InternalServerErrorException(exception.getMessage());
    }

    /*
     * This logic can be improved further. Github guarantees created_at, so if pushed_at is null we can use created_at.
     * If something goes wrong with parsing we default to minimum date that github repo was created.
     * Alternatively, we can ignore this repository.
     * */
    private Instant getUpdatedAtInstant(Repository repository) {
        Instant instant = null;
        var lastUpdated = repository.pushed_at() == null ? repository.created_at() : repository.pushed_at();
        try {
            instant = Instant.parse(lastUpdated);
        } catch (DateTimeParseException ex) {
            log.error("Could not parse the pushed at property: {} ", lastUpdated);
        }
        return instant == null ? MIN_GITHUB_PUSHED_AT : instant;
    }
}
