package com.github.repo.scorer.service;

import com.github.repo.scorer.config.RepositoryScorerConfigurationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeightedRepositoryScorerTest {

    private WeightedRepositoryScorer scorer;
    private RepositoryScorerConfigurationProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RepositoryScorerConfigurationProperties(0.5, 0.3, 0.2);
        scorer = new WeightedRepositoryScorer(properties);
    }

    @Test
    void testScoreCalculation_basic() {
        int stars = 10;
        int forks = 5;
        int daysAgo = 2;
        double expectedScore = calculateExpectedScore(stars, forks, daysAgo);

        double score = scorer.calculateScore(stars, forks, daysAgo);

        assertEquals(expectedScore, score);
    }

    @Test
    void testScoreCalculation_noStarsNoForks() {
        int stars = 0;
        int forks = 0;
        int daysAgo = 9;
        double expectedScore = calculateExpectedScore(stars, forks, daysAgo);

        double score = scorer.calculateScore(stars, forks, daysAgo);

        assertEquals(expectedScore, score);
    }

    @Test
    void testScoreCalculation_longTimeSinceUpdate() {
        int stars = 5;
        int forks = 3;
        int daysAgo = 5890;
        double expectedScore = calculateExpectedScore(stars, forks, daysAgo);

        double score = scorer.calculateScore(stars, forks, daysAgo);

        assertEquals(expectedScore, score);
    }

    private double calculateExpectedScore(int stars, int forks, int daysAgo) {
        double expected = Math.log(stars + 1) * properties.starsWeight()
                + Math.log(forks + 1) * properties.forksWeight()
                + properties.updatedAtWeight() / (1 + daysAgo);
        BigDecimal bd = new BigDecimal(expected).setScale(2, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }
}
