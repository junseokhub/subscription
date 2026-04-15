package com.api.subscription.subscription.controller;

import com.api.subscription.common.response.ApiResponse;
import com.api.subscription.subscription.dto.HistoryResponse;
import com.api.subscription.subscription.dto.SubscriptionRequest;
import com.api.subscription.subscription.dto.SubscriptionResponse;
import com.api.subscription.subscription.service.SubscriptionHistoryService;
import com.api.subscription.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionHistoryService subscriptionHistoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionResponse>> subscribe(
            @Valid @RequestBody SubscriptionRequest request
    ) {
        SubscriptionResponse response = subscriptionService.subscribe(
                request.phoneNumber(),
                request.channelId(),
                request.status()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<SubscriptionResponse>> unsubscribe(
            @Valid @RequestBody SubscriptionRequest request
    ) {
        SubscriptionResponse response = subscriptionService.unsubscribe(
                request.phoneNumber(),
                request.channelId(),
                request.status()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<HistoryResponse>> getHistory(
            @RequestParam String phoneNumber
    ) {
        HistoryResponse response = subscriptionHistoryService.getHistory(phoneNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}