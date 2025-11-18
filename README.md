# Github Repository Scorer

> This project calculates a popularity score for GitHub repositories by analyzing key metrics such as stars, forks, and
> the recency of updates. The scoring system identifies the most active and popular repositories, making it faster to
> find relevant projects. It provides a Search Repositories API that is paginated and allows users to filter results by
> programming language and creation date. It returns repositories with score sorted by score value in descending order.
>
> Scoring uses weighted scoring strategy, where following weights have been assigned to each criteria
> - Stars – logarithmically weighted.(0.5)
> - Forks – logarithmically weighted.(0.3)
> - Recency of updates – weighted inversely by days since last update.(0.2)
>
> It uses following formula:
> - score = 0.5 * log(stars + 1) + 0.3 * log(forks + 1) + 0.2 / (days_since_last_update + 1)

---

## Assumptions

- The GitHub Search API provides two date fields: updated_at and pushed_at. The updated_at field reflects when a
  repository was last updated, which can include changes to repository metadata and may not indicate actual code
  changes. In contrast, pushed_at reflects the timestamp of the last code push. Since our goal is to rank repositories
  based on recent update activity, we consider pushed_at to be a more accurate indicator of activity.
- Since the goal is to assess repository popularity, mirrored and archived repositories are excluded, as they do
  not reflect active development or genuine user engagement. This can be configured in the service.
- Currently, requests to the GitHub Search API are unauthenticated, so the application can only access public
  repositories.
- Weights have been assigned to prioritize stars, forks, and the recency of updates, in that order. Additionally, the
  GitHub Search API request is configured to return results sorted by stars in descending order to align with this
  prioritization.

---

## Improvements

- Currently, nothing is being stored in a database, and scores are calculated on the fly. Storing repository data and
  calculated scores in a database would allow persistence, faster queries, and avoid recalculating scores each time the
  application runs.
- The GitHub Search API may return HTTP 304 (Not Modified) for repeated requests. To improve efficiency and reduce
  unnecessary network calls, it is recommended to cache this data.
- Since the GitHub Search API is rate-limited, it’s important to implement request logic that respects these
  limits. The application should notify users clearly when the limit is reached and suggest trying again later,
  preventing failed requests and improving the user experience.
- Retry transient errors (e.g., 500, 502, 503) with exponential backoff.
- Implement circuit breakers to avoid overwhelming the API when it’s down.
- Use authenticated token to avoid rate limits or access private repositories.

---

## Prerequisites

- Docker must be installed

---

## How to build and run the application

Follow these steps to set up and run the Spring Boot GitHub scoring application:

1. **Clone the repository**
   ```bash
   git clone https://github.com/npastor/github-repo-scorer.git
   cd github-repo-scorer
2. **Build the docker image**
   ```bash
   docker build -t github-repo-scorer .
3. **Run the docker container**
   ```bash
   docker run -p 8080:8080 github-repo-scorer
4. **Access the API**
    - Open the Swagger UI in your browser to explore and call the REST APIs:
        - http://localhost:8080/swagger-ui/index.html
        - Click on the api and then click on `Try it out` button on the right side.
        - This API is paginated, default params are page_size = 100 and page = 1
        - It also returns results sorted by score(desc)
    - Alternatively, you can use Postman or curl to test the API endpoint.
        - GET http://localhost:8080/api/v1/repositories?language=java&created_after=2010-11-01&page=1&page_size=100
        - curl -X
          GET "http://localhost:8080/api/v1/repositories?language=java&created_after=2010-11-01&page=1&page_size=100"

5. **To run tests**
    - cd github-repo-scorer
    - mvn test

---

## How it Works

The application calculates a score for each repository based on:

    Stars – logarithmically weighted. (0.5)
    Forks – logarithmically weighted. (0.3)
    Recency of updates – weighted inversely by days since last update. (0.2)

Weights have been assigned to prioritize stars, forks, and the recency of updates, in that order. These settings are
configured in the application.properties file and can be adjusted as needed.

Scores are rounded to two decimal places for
simplicity.

Only active, original repositories are considered in the scoring.
