package com.urlshortener.service;

import com.urlshortener.dto.UrlRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.entity.Url;
import com.urlshortener.exception.UrlExpiredException;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.mapper.UrlMapper;
import com.urlshortener.repository.UrlRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final UrlMapper urlMapper;

    @Value("${app.short-code-length}")
    private int shortCodeLength;

    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional
    public UrlResponse shortenUrl(UrlRequest request) {
        String shortCode = generateUniqueShortCode();
        Url saved = urlRepository.save(urlMapper.toEntity(request, shortCode));
        return urlMapper.toResponse(saved);
    }

    @Transactional
    public String resolveUrl(String shortCode) {
        Url url = findByShortCodeOrThrow(shortCode);

        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException(shortCode);
        }

        urlRepository.incrementClickCount(shortCode);
        return url.getOriginalUrl();
    }

    public UrlResponse getStats(String shortCode) {
        return urlMapper.toResponse(findByShortCodeOrThrow(shortCode));
    }

    public List<UrlResponse> getAllUrls() {
        return urlRepository.findAll()
                .stream()
                .map(urlMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUrl(String shortCode) {
        urlRepository.delete(findByShortCodeOrThrow(shortCode));
    }

    private Url findByShortCodeOrThrow(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));
    }

    private String generateUniqueShortCode() {
        String code;
        do {
            code = generateShortCode();
        } while (urlRepository.existsByShortCode(code));
        return code;
    }

    private String generateShortCode() {
        StringBuilder sb = new StringBuilder(shortCodeLength);
        for (int i = 0; i < shortCodeLength; i++) {
            sb.append(BASE62.charAt(SECURE_RANDOM.nextInt(BASE62.length())));
        }
        return sb.toString();
    }
}
