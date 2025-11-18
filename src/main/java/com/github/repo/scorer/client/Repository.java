package com.github.repo.scorer.client;

public record Repository(int id,
                         String name,
                         String description,
                         String created_at,
                         String pushed_at,
                         int forks_count,
                         int stargazers_count,
                         String language) {
}
