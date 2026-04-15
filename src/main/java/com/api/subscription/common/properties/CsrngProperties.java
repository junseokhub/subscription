package com.api.subscription.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external.csrng")
public record CsrngProperties(
        String url,
        Timeout timeout
) {
    public record Timeout(
            int connect,
            int read
    ) {}
}