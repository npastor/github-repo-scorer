package com.github.repo.scorer.model;

public record SearchRepositoryRequest(SearchRepositoryQuery query, PageRequest pageRequest) {
}
