package com.github.repo.scorer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@ConfigurationPropertiesScan("com.github.repo.scorer.config")
public class GithubRepoScorerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GithubRepoScorerApplication.class, args);
    }

}
