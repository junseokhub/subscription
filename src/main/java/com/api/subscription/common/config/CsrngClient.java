package com.api.subscription.common.config;

import com.api.subscription.common.exception.BusinessException;
import com.api.subscription.common.exception.ErrorCode;
import com.api.subscription.common.properties.CsrngProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Slf4j
@Component
@EnableConfigurationProperties(CsrngProperties.class)
public class CsrngClient {

    private final RestClient restClient;

    public CsrngClient(CsrngProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.url())
                .build();
    }

    @Retryable(includes = RestClientException.class, maxRetries = 3, delay = 1000)
    public boolean isRollback() {
        log.info("csrng API 호출");
        List<CsrngResponse> responses = restClient.get()
                .uri("?min=0&max=1")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (responses == null || responses.isEmpty()) {
            log.warn("csrng API 응답이 비어있습니다.");
            throw new BusinessException(ErrorCode.EXTERNAL_API_FAILURE);
        }

        int random = responses.get(0).random();
        log.info("csrng random 값: {}", random);
        return random == 0;
    }

    public record CsrngResponse(
            String status,
            int min,
            int max,
            int random
    ) {}
}