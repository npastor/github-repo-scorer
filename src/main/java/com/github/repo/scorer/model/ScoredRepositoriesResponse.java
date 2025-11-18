package com.github.repo.scorer.model;

import java.util.List;

public record ScoredRepositoriesResponse(int total_count,
                                         int page,
                                         int page_size,
                                         List<ScoredRepository> repositories) {
}
