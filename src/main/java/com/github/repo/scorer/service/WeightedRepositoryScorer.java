package com.github.repo.scorer.service;

import com.github.repo.scorer.config.RepositoryScorerConfigurationProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class WeightedRepositoryScorer implements RepositoryScorer {

    private final RepositoryScorerConfigurationProperties properties;

    public WeightedRepositoryScorer(RepositoryScorerConfigurationProperties properties) {
        this.properties = properties;
    }

    @Override
    public double calculateScore(int stars, int forks, long daysSinceLastUpdated) {
        if (properties.starsWeight() < 0 || properties.forksWeight() < 0 || properties.updatedAtWeight() < 0) {
            throw new IllegalArgumentException("Negative weights are not allowed");
        }
        double starsScore = Math.log(stars + 1) * properties.starsWeight();
        double forksScore = Math.log(forks + 1) * properties.forksWeight();

        double updatedAtScore = properties.updatedAtWeight() / (1 + daysSinceLastUpdated);

        BigDecimal roundedScore = BigDecimal.valueOf(starsScore + forksScore + updatedAtScore);
        roundedScore = roundedScore.setScale(2, RoundingMode.HALF_UP);
        return roundedScore.doubleValue();
    }
}
