package com.github.repo.scorer.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchQueryBuilderTest {
    @Test
    void testBasicQuery() {
        SearchQueryBuilder query = new SearchQueryBuilder.Builder("java", "2020-01-01")
                .build();
        String expected = "language:java created:>2020-01-01 archived:false mirror:false";
        assertEquals(expected, query.buildQueryString());
    }

    @Test
    void testBlankLanguage() {
        SearchQueryBuilder query = new SearchQueryBuilder.Builder("", "2020-01-01")
                .build();
        String expected = "created:>2020-01-01 archived:false mirror:false";
        assertEquals(expected, query.buildQueryString());
    }

    @Test
    void testNullLanguage() {
        SearchQueryBuilder query = new SearchQueryBuilder.Builder(null, "2020-01-01")
                .build();
        String expected = "created:>2020-01-01 archived:false mirror:false";
        assertEquals(expected, query.buildQueryString());
    }

    @Test
    void testNullCreatedAfter() {
        SearchQueryBuilder query = new SearchQueryBuilder.Builder("java", null)
                .build();
        String expected = "language:java archived:false mirror:false";
        assertEquals(expected, query.buildQueryString());
    }

    @Test
    void testCustomFlags() {
        SearchQueryBuilder query = new SearchQueryBuilder.Builder("java", "2020-01-01")
                .archived(true)
                .mirror(true)
                .build();
        String expected = "language:java created:>2020-01-01 archived:true mirror:true";
        assertEquals(expected, query.buildQueryString());
    }

    @Test
    void testBlankLanguageAndCreatedAfter() {
        SearchQueryBuilder query = new SearchQueryBuilder.Builder("", "")
                .archived(true)
                .mirror(false)
                .build();
        String expected = "archived:true mirror:false";
        assertEquals(expected, query.buildQueryString());
    }
}