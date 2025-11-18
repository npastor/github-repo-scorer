package com.github.repo.scorer.controller;

import com.github.repo.scorer.model.PageRequest;
import com.github.repo.scorer.model.ScoredRepositoriesResponse;
import com.github.repo.scorer.model.SearchRepositoryQuery;
import com.github.repo.scorer.model.SearchRepositoryRequest;
import com.github.repo.scorer.service.RepositorySearchService;
import jakarta.validation.constraints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
@Validated
public class RepositoryController {
    private static final Logger log = LoggerFactory.getLogger(RepositoryController.class);
    private final RepositorySearchService repositorySearchService;

    public RepositoryController(RepositorySearchService repositorySearchService) {
        this.repositorySearchService = repositorySearchService;
    }

    @GetMapping("/repositories")
    public ResponseEntity<ScoredRepositoriesResponse> getRepositories(
            @RequestParam @NotBlank @Size(max = 50)
            String language,
            @RequestParam(name = "created_after")
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate createdAfter,
            @RequestParam(required = false, defaultValue = "1") @Min(1)
            Integer page,
            @RequestParam(name = "page_size", required = false, defaultValue = "100") @Min(1) @Max(value = 100)
            Integer pageSize
    ) {
        var searchRequest = new SearchRepositoryRequest(
                new SearchRepositoryQuery(language, createdAfter.toString()),
                new PageRequest(pageSize, page));

        log.info("Received request to return scored repositories: {}", searchRequest);

        return ResponseEntity.ok(repositorySearchService.searchAndScore(searchRequest));
    }
}
