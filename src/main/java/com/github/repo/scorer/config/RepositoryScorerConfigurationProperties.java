package com.github.repo.scorer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "repository.scorer")
public record RepositoryScorerConfigurationProperties(double starsWeight, double forksWeight, double updatedAtWeight) {
}
