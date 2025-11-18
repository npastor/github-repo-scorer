package com.github.repo.scorer.service;

public interface RepositoryScorer {
    double calculateScore(int stars, int forks, long daysSinceLastUpdated);
}
