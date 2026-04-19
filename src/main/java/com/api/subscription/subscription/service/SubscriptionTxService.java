package com.api.subscription.subscription.service;

import com.api.subscription.channel.domain.Channel;
import com.api.subscription.common.exception.BusinessException;
import com.api.subscription.common.exception.ErrorCode;
import com.api.subscription.member.domain.Member;
import com.api.subscription.member.service.MemberService;
import com.api.subscription.subscription.domain.ActionType;
import com.api.subscription.subscription.domain.Subscription;
import com.api.subscription.subscription.domain.SubscriptionHistory;
import com.api.subscription.subscription.domain.SubscriptionStatus;
import com.api.subscription.subscription.dto.SubscriptionResponse;
import com.api.subscription.subscription.repository.SubscriptionHistoryRepository;
import com.api.subscription.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionTxService {
    private final MemberService memberService;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;

    @Transactional
    public SubscriptionResponse proceedSubscription(String phoneNumber, Channel channel, SubscriptionStatus newStatus) {
        // 멤버 조회 및 생성
        Member member = memberService.getOrCreateMember(phoneNumber);

        Subscription subscription = subscriptionRepository.findByMember(member).orElse(null);
        ActionType actionType;

        if (subscription == null) {
            validateSubscribeStatus(null, newStatus);
            subscription = subscriptionRepository.save(
                    Subscription.builder().member(member).status(newStatus).build()
            );
            actionType = ActionType.SUBSCRIBE;
        } else {
            SubscriptionStatus previousStatus = subscription.getStatus();
            validateSubscribeStatus(previousStatus, newStatus);
            subscription.changeStatus(newStatus);
            actionType = determineActionType(previousStatus, newStatus);
        }

        // 히스토리 저장
        subscriptionHistoryRepository.save(
                SubscriptionHistory.builder()
                        .member(member)
                        .channel(channel)
                        .actionType(actionType)
                        .changedStatus(newStatus)
                        .build()
        );

        return SubscriptionResponse.of(phoneNumber, newStatus);
    }

    // 구독 변경 활동 상태
    private ActionType determineActionType(SubscriptionStatus previous, SubscriptionStatus newStatus) {
        if (previous == SubscriptionStatus.NORMAL && newStatus == SubscriptionStatus.PREMIUM) {
            return ActionType.UPGRADE;
        }
        return ActionType.SUBSCRIBE;
    }

    // 구독 변경 검증
    private void validateSubscribeStatus(SubscriptionStatus current, SubscriptionStatus newStatus) {
        if (current == null) return;

        switch (current) {
            case NONE -> {
                if (newStatus == SubscriptionStatus.NONE) {
                    throw new BusinessException(ErrorCode.INVALID_SUBSCRIPTION_STATUS);
                }
            }
            case NORMAL -> {
                if (newStatus != SubscriptionStatus.PREMIUM) {
                    throw new BusinessException(ErrorCode.INVALID_SUBSCRIPTION_STATUS);
                }
            }
            case PREMIUM -> throw new BusinessException(ErrorCode.ALREADY_PREMIUM);
        }
    }
}