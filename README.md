# URL Shortener — REST API

A production-ready URL Shortener backend built with **Spring Boot 3**, **Java 17**, and **Spring Data JPA**. Converts long URLs into short, shareable links with click tracking and expiry support.

---

## What This Project Does

- Takes a long URL (e.g., `https://www.example.com/very/long/path`) and returns a short code (e.g., `http://localhost:8080/aB3xYz`)
- Redirects users from the short URL to the original URL
- Tracks how many times each short link is clicked
- Supports optional expiry dates on links
- Provides analytics stats per short URL

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Database | H2 (dev) / MySQL (production) |
| ORM | Spring Data JPA + Hibernate |
| Security | Spring Security + API Key Filter |
| Validation | Spring Boot Validation (Jakarta) |
| Build Tool | Maven |
| Utilities | Lombok |

---

## Project Structure

```
url-shortener/
├── src/
│   ├── main/
│   │   ├── java/com/urlshortener/
│   │   │   ├── UrlShortenerApplication.java      # Entry point
│   │   │   ├── controller/
│   │   │   │   └── UrlController.java             # REST endpoints
│   │   │   ├── service/
│   │   │   │   └── UrlService.java                # Business logic
│   │   │   ├── repository/
│   │   │   │   └── UrlRepository.java             # Database access
│   │   │   ├── entity/
│   │   │   │   └── Url.java                       # JPA entity (DB table)
│   │   │   ├── dto/
│   │   │   │   ├── UrlRequest.java                # Input model
│   │   │   │   ├── UrlResponse.java               # Output model
│   │   │   │   └── ErrorResponse.java             # Consistent error shape
│   │   │   ├── mapper/
│   │   │   │   └── UrlMapper.java                 # Single place for entity ↔ DTO conversion
│   │   │   ├── exception/
│   │   │   │   ├── UrlNotFoundException.java      # 404 — short code missing
│   │   │   │   └── UrlExpiredException.java       # 410 — link expired
│   │   │   └── config/
│   │   │       ├── ApiKeyFilter.java              # Security — API key enforcement
│   │   │       ├── SecurityConfig.java            # Spring Security filter chain
│   │   │       └── GlobalExceptionHandler.java    # Centralized error handling
│   │   └── resources/
│   │       └── application.properties             # Config + API key
│   └── test/
│       └── java/com/urlshortener/
│           └── UrlShortenerApplicationTests.java
└── pom.xml
```

---

## API Endpoints

### 1. Shorten a URL
```
POST /api/shorten
Content-Type: application/json

{
  "originalUrl": "https://www.google.com",
  "expiryDays": 7
}
```
**Response:**
```json
{
  "shortCode": "aB3xYz",
  "shortUrl": "http://localhost:8080/aB3xYz",
  "originalUrl": "https://www.google.com",
  "clickCount": 0,
  "createdAt": "2024-01-01T10:00:00",
  "expiresAt": "2024-01-08T10:00:00"
}
```

---

### 2. Redirect to Original URL
```
GET /{shortCode}
```
Automatically redirects (HTTP 302) to the original URL and increments click count.

---

### 3. Get Click Statistics
```
GET /api/stats/{shortCode}
```
Returns full URL details including total click count.

---

### 4. List All URLs
```
GET /api/urls
```
Returns all shortened URLs with their stats.

---

### 5. Delete a Short URL
```
DELETE /api/urls/{shortCode}
```
Permanently removes the short URL.

---

## How to Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Steps
```bash
# Clone or navigate to the project
cd url-shortener

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

App starts at: `http://localhost:8080`

H2 Console (dev only): `http://localhost:8080/h2-console`

---

## How to Test with curl

```bash
# Shorten a URL
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://www.github.com"}'

# Get stats (replace aB3xYz with actual short code)
curl http://localhost:8080/api/stats/aB3xYz

# List all URLs
curl http://localhost:8080/api/urls

# Delete a URL
curl -X DELETE http://localhost:8080/api/urls/aB3xYz
```

---

## Switch to MySQL (Production)

Update `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/urldb
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
```

---

## Security

Admin endpoints (`/api/urls`, `/api/stats/{code}`) require the `X-API-Key` header:

```bash
# Protected endpoint — must include API key
curl http://localhost:8080/api/urls \
  -H "X-API-Key: my-secret-admin-key-123"

# Public endpoint — no key needed
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://www.github.com"}'
```

Change `app.admin-api-key` in `application.properties` (or set as an environment variable in production).

---

## Error Response Format

All errors return a consistent JSON structure:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Short URL not found: xyz123",
  "timestamp": "2024-01-01T10:00:00"
}
```

| HTTP Status | Scenario |
|---|---|
| 400 | Invalid request body / validation failure |
| 401 | Missing or wrong API key |
| 404 | Short code does not exist |
| 410 | Short URL has expired |
| 500 | Unexpected server error |

---

## Key Concepts You Learn From This Project

- **REST API design** — proper HTTP methods, status codes, request/response structure
- **Spring Boot layers** — Controller → Service → Repository pattern
- **JPA & Hibernate** — entity mapping, JPQL queries, transactions
- **Input validation** — Jakarta Bean Validation annotations
- **Mapper pattern** — single `UrlMapper` class owns all entity ↔ DTO conversion; no duplication across classes
- **Custom exceptions** — typed exceptions (`UrlNotFoundException`, `UrlExpiredException`) instead of raw `RuntimeException`
- **Global exception handling** — one handler maps every exception type to a consistent `ErrorResponse`
- **Spring Security** — stateless API key filter protecting admin routes
- **Base62 encoding** — how short codes are generated
- **Database design** — simple but real-world table design

---

## Run Tests

```bash
mvn test
```
