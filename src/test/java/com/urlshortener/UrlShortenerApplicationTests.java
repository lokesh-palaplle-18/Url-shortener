package com.urlshortener;

import com.urlshortener.dto.UrlRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.service.UrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class UrlShortenerApplicationTests {

    @Autowired
    private UrlService urlService;

    @Test
    void contextLoads() {
    }

    @Test
    void shouldShortenUrl() {
        UrlRequest request = new UrlRequest();
        request.setOriginalUrl("https://www.google.com");

        UrlResponse response = urlService.shortenUrl(request);

        assertThat(response.getShortCode()).isNotNull();
        assertThat(response.getShortCode()).hasSize(6);
        assertThat(response.getOriginalUrl()).isEqualTo("https://www.google.com");
        assertThat(response.getClickCount()).isEqualTo(0L);
    }

    @Test
    void shouldResolveShortCode() {
        UrlRequest request = new UrlRequest();
        request.setOriginalUrl("https://www.github.com");

        UrlResponse response = urlService.shortenUrl(request);
        String resolved = urlService.resolveUrl(response.getShortCode());

        assertThat(resolved).isEqualTo("https://www.github.com");
    }

    @Test
    void shouldIncrementClickCount() {
        UrlRequest request = new UrlRequest();
        request.setOriginalUrl("https://www.example.com");

        UrlResponse response = urlService.shortenUrl(request);
        urlService.resolveUrl(response.getShortCode());
        urlService.resolveUrl(response.getShortCode());

        UrlResponse stats = urlService.getStats(response.getShortCode());
        assertThat(stats.getClickCount()).isEqualTo(2L);
    }

    @Test
    void shouldThrowForInvalidShortCode() {
        assertThrows(RuntimeException.class, () -> urlService.resolveUrl("invalid123"));
    }
}
