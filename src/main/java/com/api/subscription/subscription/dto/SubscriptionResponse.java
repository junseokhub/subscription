package com.api.subscription.subscription.dto;

import com.api.subscription.subscription.domain.SubscriptionStatus;

public record SubscriptionResponse(
        String phoneNumber,
        SubscriptionStatus status
) {
    public static SubscriptionResponse of(String phoneNumber, SubscriptionStatus status) {
        return new SubscriptionResponse(phoneNumber, status);
    }
}