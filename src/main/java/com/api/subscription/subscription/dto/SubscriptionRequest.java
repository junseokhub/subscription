package com.api.subscription.subscription.dto;

import com.api.subscription.subscription.domain.SubscriptionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SubscriptionRequest(

        @NotBlank(message = "휴대폰번호는 필수입니다.")
        @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰번호 형식이 아닙니다.")
        String phoneNumber,

        @NotNull(message = "채널 ID 는 필수입니다.")
        Long channelId,

        @NotNull(message = "구독 상태는 필수입니다.")
        SubscriptionStatus status
) {}