package com.github.repo.scorer.service;

import com.github.repo.scorer.config.RepositoryScorerConfigurationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeightedRepositoryScorerTest {

    private WeightedRepositoryScorer scorer;
    private RepositoryScorerConfigurationProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RepositoryScorerConfigurationProperties(0.5, 0.3, 0.2);
        scorer = new WeightedRepositoryScorer(properties);
    }

    @Test
    void testAndCompareScoreWhenOneRepoHasMoreStars() {
        double lessStarsScore = scorer.calculateScore(200, 10, 2);
        double highStarsScore = scorer.calculateScore(500, 10, 2);

        assertTrue(highStarsScore > lessStarsScore);
    }

    @Test
    void testAndCompareScoreWhenOneRepoHasMoreForks() {
        double lessForksScore = scorer.calculateScore(500, 9, 2);
        double highForksScore = scorer.calculateScore(500, 10, 2);

        assertTrue(highForksScore > lessForksScore);
    }

    @Test
    void testAndCompareScoreWhenOneRepoHasMoreDaysAgo() {
        double lessDaysAgoScore = scorer.calculateScore(500, 5, 2);
        double highDaysAgoScore = scorer.calculateScore(500, 5, 5);

        assertTrue(highDaysAgoScore < lessDaysAgoScore); // as this is inverse, we want more recently updated repo
    }

    @Test
    void testScoreWithZeroValues() {
        double zeroScore = scorer.calculateScore(0, 0, 0);
        double normalScore = scorer.calculateScore(100, 1, 1);

        assertTrue(normalScore > zeroScore);
    }

    @Test
    void testScoreWithEqualValues() {
        double score1 = scorer.calculateScore(500, 5, 2);
        double score2 = scorer.calculateScore(500, 5, 2);

        assertEquals(score1, score2);
    }

    @Test
    void testScoreCombination() {
        double scoreA = scorer.calculateScore(500, 10, 2); // more forks, fewer days
        double scoreB = scorer.calculateScore(600, 5, 5);  // more stars, fewer forks, older

        assertTrue(scoreA > scoreB);
    }

    @Test
    void testNegativeStarsWeightThrowException() {
        properties = new RepositoryScorerConfigurationProperties(-0.5, 0.3, 0.2);
        scorer = new WeightedRepositoryScorer(properties);

        assertThrows(IllegalArgumentException.class, () -> {
            scorer.calculateScore(500, 10, 2);
        });
    }

    @Test
    void testNegativeForksWeightThrowException() {
        properties = new RepositoryScorerConfigurationProperties(0.5, -0.3, 0.2);
        scorer = new WeightedRepositoryScorer(properties);

        assertThrows(IllegalArgumentException.class, () -> {
            scorer.calculateScore(500, 10, 2);
        });
    }

    @Test
    void testNegativeUpdatedAtWeightThrowException() {
        properties = new RepositoryScorerConfigurationProperties(0.5, 0.3, -0.2);
        scorer = new WeightedRepositoryScorer(properties);

        assertThrows(IllegalArgumentException.class, () -> {
            scorer.calculateScore(500, 10, 2);
        });
    }
}
