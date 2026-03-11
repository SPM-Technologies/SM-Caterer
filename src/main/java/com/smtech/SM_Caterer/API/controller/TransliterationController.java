package com.smtech.SM_Caterer.API.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Proxy controller for Google Input Tools transliteration API.
 * Avoids CORS issues by proxying requests through the server.
 * Public endpoint - no authentication required.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/transliterate")
public class TransliterationController {

    private static final String GOOGLE_API = "https://inputtools.google.com/request";
    private static final int MAX_SUGGESTIONS = 8;
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    /**
     * Proxy transliteration request to Google Input Tools.
     *
     * @param text the English text to transliterate
     * @param lang the target language code (hi or mr)
     * @return JSON response from Google Input Tools
     */
    @GetMapping
    public ResponseEntity<String> transliterate(
            @RequestParam String text,
            @RequestParam(defaultValue = "mr") String lang) {

        if (text == null || text.isBlank() || text.length() > 100) {
            return ResponseEntity.badRequest().body("[\"ERROR\"]");
        }

        // Only allow hi and mr
        String itc;
        switch (lang) {
            case "hi": itc = "hi-t-i0-und"; break;
            case "mr": itc = "mr-t-i0-und"; break;
            default: return ResponseEntity.badRequest().body("[\"ERROR\"]");
        }

        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = GOOGLE_API + "?text=" + encodedText +
                    "&itc=" + itc +
                    "&num=" + MAX_SUGGESTIONS +
                    "&cp=0&cs=1&ie=utf-8&oe=utf-8&app=demopage";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            return ResponseEntity.ok()
                    .header("Content-Type", "application/json; charset=utf-8")
                    .body(response.body());

        } catch (Exception e) {
            log.warn("Transliteration API call failed: {}", e.getMessage());
            return ResponseEntity.ok().body("[\"ERROR\"]");
        }
    }
}
