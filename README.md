# Github Repository Scorer

> This project calculates a popularity score for GitHub repositories by analyzing key metrics such as stars, forks, and
> the recency of updates. The scoring system helps identify the most active and popular repositories quickly and
> efficiently.

---

## Assumptions

- Since the goal is to assess repository popularity, mirrored and archived repositories are excluded, as they do
  not reflect active development or genuine user engagement.

---

## Improvements

- List areas where the project could be improved.
- The GitHub Search API may return HTTP 304 (Not Modified) for repeated requests. To improve efficiency and reduce
  unnecessary network calls, it is recommended to cache repository data.
- Since the GitHub Search API is rate-limited, it’s important to implement request logic that respects these
  limits and provides users with a clear message to try again later when the limit is reached.
- Example: "Add Docker support for easier deployment."

---

## How to Setup

Follow these steps to set up and run the Spring Boot GitHub scoring application:

1. **Clone the repository**
   ```basH
   git clone https://github.com/yourusername/github-repo-scorer.git
   cd github-repo-scorer
2. **Build the docker image**
   ```basH
   docker build -t github-repo-scorer .
3. **Run the docker container**
   ```basH
   docker run -p 8080:8080 github-repo-scorer
4. **Access the API**
    - Open the Swagger UI in your browser to explore and call the REST APIs:
        - http://localhost:8080/swagger-ui/index.html
    - Alternatively, you can use Postman to test the API endpoint.
        - http://localhost:8080/api/v1/repositories?language=java&created_after=2010-11-01&page=1&page_size=100

---

## How it Works

The application calculates a score for each repository based on:

    Stars – logarithmically weighted.
    Forks – logarithmically weighted.
    Recency of updates – weighted inversely by days since last update.

Scores are rounded to two decimal places for simplicity.
Only active, original repositories are considered in the scoring.