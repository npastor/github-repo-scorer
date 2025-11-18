package com.github.repo.scorer.client;

import com.github.repo.scorer.config.GithubFeignClientConfig;
import com.github.repo.scorer.model.SearchRepositoriesResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "githubClient", url = "${github.api.url}", configuration = GithubFeignClientConfig.class)
public interface GithubFeignClient {

    @GetMapping("${github.api.search-path}")
    SearchRepositoriesResponse searchRepositories(
            @RequestParam("q") String query,
            @RequestParam("per_page") int perPage,
            @RequestParam("page") int page,
            @RequestParam("sort") String sort,
            @RequestParam("order") String order

    );
}
