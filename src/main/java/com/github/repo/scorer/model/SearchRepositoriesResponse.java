package com.github.repo.scorer.model;

import com.github.repo.scorer.client.Repository;

import java.util.List;

public record SearchRepositoriesResponse(int total_count,
                                         List<Repository> items) {
}
