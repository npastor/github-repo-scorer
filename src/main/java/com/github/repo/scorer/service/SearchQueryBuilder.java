package com.github.repo.scorer.service;

public class SearchQueryBuilder {
    private final String language;
    private final String createdAfter;
    private final boolean archived;
    private final boolean mirror;

    private SearchQueryBuilder(Builder builder) {
        this.language = builder.language;
        this.createdAfter = builder.createdAfter;
        this.archived = builder.archived;
        this.mirror = builder.mirror;
    }

    public String buildQueryString() {
        StringBuilder query = new StringBuilder();
        if (language != null && !language.isBlank()) {
            query.append("language:").append(language).append(" ");
        }

        if (createdAfter != null && !createdAfter.isBlank()) {
            query.append("created:>").append(createdAfter).append(" ");
        }

        query.append("archived:").append(archived).append(" ");
        query.append("mirror:").append(mirror);

        return query.toString();
    }

    public static class Builder {
        private final String language;
        private final String createdAfter;
        private boolean archived = false;
        private boolean mirror = false;

        public Builder(String language, String createdAfter) {
            this.language = language;
            this.createdAfter = createdAfter;
        }

        public Builder archived(boolean archived) {
            this.archived = archived;
            return this;
        }

        public Builder mirror(boolean mirror) {
            this.mirror = mirror;
            return this;
        }

        public SearchQueryBuilder build() {
            return new SearchQueryBuilder(this);
        }
    }
}