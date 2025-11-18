package com.github.repo.scorer.service;

import com.github.repo.scorer.model.ScoredRepositoriesResponse;
import com.github.repo.scorer.model.SearchRepositoryRequest;

public interface RepositorySearchService {
    ScoredRepositoriesResponse searchAndScore(SearchRepositoryRequest request);
}
