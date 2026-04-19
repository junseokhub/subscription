package com.api.subscription.common.config;

import com.api.subscription.common.exception.BusinessException;
import com.api.subscription.common.exception.ErrorCode;
import com.api.subscription.common.properties.CsrngProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;

import static java.net.http.HttpClient.*;

@Slf4j
@Component
@EnableConfigurationProperties(CsrngProperties.class)
public class CsrngClient {

    private final RestClient restClient;

    public CsrngClient(CsrngProperties properties) {
        var httpClient = newBuilder()
                .connectTimeout(Duration.ofMillis(properties.timeout().connect()))
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(properties.timeout().read()));
        this.restClient = RestClient.builder()
                .baseUrl(properties.url())
                .build();
    }

    @Retryable(includes = RestClientException.class, maxRetries = 3, delay = 1000)
    public boolean canProceed() {
        log.info("구독 가능 여부 확인을 위한 외부 API 호출");

        List<CsrngResponse> responses = restClient.get()
                .uri("?min=0&max=1")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (responses == null || responses.isEmpty()) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_FAILURE);
        }

        int randomValue = responses.getFirst().random();
        return randomValue == 0;
    }

    public record CsrngResponse(
            String status,
            int min,
            int max,
            int random
    ) {}
}