package com.github.repo.scorer.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GithubFeignClientConfig {

    private static final String ACCEPT_HEADER_KEY = "Accept";
    private static final String ACCEPT_HEADER_VALUE = "application/vnd.github+json";

    @Bean
    public RequestInterceptor githubAcceptHeaderInterceptor() {
        return requestTemplate -> requestTemplate.header(ACCEPT_HEADER_KEY, ACCEPT_HEADER_VALUE);
    }
}
