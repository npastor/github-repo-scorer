package com.github.repo.scorer.model;

public record ScoredRepository(Integer id, String name, String description, double score, String language,
                               String created_at, String updated_at, int stars, int forks) {
}
