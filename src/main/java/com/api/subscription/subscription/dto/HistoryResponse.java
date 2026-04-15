package com.api.subscription.subscription.dto;

import com.api.subscription.subscription.domain.ActionType;
import com.api.subscription.subscription.domain.SubscriptionHistory;
import com.api.subscription.subscription.domain.SubscriptionStatus;

import java.time.LocalDateTime;
import java.util.List;

public record HistoryResponse(
        List<HistoryItem> history,
        String summary
) {
    public record HistoryItem(
            String channelName,
            ActionType actionType,
            SubscriptionStatus changedStatus,
            LocalDateTime createdAt
    ) {
        public static HistoryItem from(SubscriptionHistory history) {
            return new HistoryItem(
                    history.getChannel().getName(),
                    history.getActionType(),
                    history.getChangedStatus(),
                    history.getCreatedAt()
            );
        }
    }

    public static HistoryResponse of(List<SubscriptionHistory> histories, String summary) {
        return new HistoryResponse(
                histories.stream()
                        .map(HistoryItem::from)
                        .toList(),
                summary
        );
    }
}