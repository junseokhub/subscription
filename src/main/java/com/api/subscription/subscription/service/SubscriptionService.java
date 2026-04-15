package com.api.subscription.subscription.service;

import com.api.subscription.channel.domain.Channel;
import com.api.subscription.channel.domain.ChannelType;
import com.api.subscription.channel.repository.ChannelRepository;
import com.api.subscription.common.config.CsrngClient;
import com.api.subscription.common.exception.BusinessException;
import com.api.subscription.common.exception.ErrorCode;
import com.api.subscription.member.domain.Member;
import com.api.subscription.member.repository.MemberRepository;
import com.api.subscription.subscription.domain.ActionType;
import com.api.subscription.subscription.domain.Subscription;
import com.api.subscription.subscription.domain.SubscriptionHistory;
import com.api.subscription.subscription.domain.SubscriptionStatus;
import com.api.subscription.subscription.dto.SubscriptionResponse;
import com.api.subscription.subscription.repository.SubscriptionHistoryRepository;
import com.api.subscription.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final MemberRepository memberRepository;
    private final ChannelRepository channelRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final CsrngClient csrngClient;


    // 구독
    @Transactional
    public SubscriptionResponse subscribe(String phoneNumber, Long channelId, SubscriptionStatus newStatus) {

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        if (channel.getType() == ChannelType.UNSUBSCRIBE) {
            throw new BusinessException(ErrorCode.CHANNEL_CANNOT_SUBSCRIBE);
        }

        Member member = memberRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .phoneNumber(phoneNumber)
                                .build()
                ));

        Subscription subscription = subscriptionRepository.findByMember(member)
                .orElse(null);

        ActionType actionType;

        if (subscription == null) {
            validateSubscribeStatus(null, newStatus);
            subscription = subscriptionRepository.save(
                    Subscription.builder()
                            .member(member)
                            .status(newStatus)
                            .build()
            );
            actionType = ActionType.SUBSCRIBE;
        } else {
            SubscriptionStatus previousStatus = subscription.getStatus();
            validateSubscribeStatus(previousStatus, newStatus);
            subscription.changeStatus(newStatus);
            actionType = determineActionType(previousStatus, newStatus);
        }

        if (!csrngClient.isRollback()) {
            log.info("트랜잭션 롤백, phoneNumber: {}", phoneNumber);
            throw new BusinessException(ErrorCode.TRANSACTION_ROLLBACK);
        }

        subscriptionHistoryRepository.save(
                SubscriptionHistory.builder()
                        .member(member)
                        .channel(channel)
                        .actionType(actionType)
                        .changedStatus(newStatus)
                        .build()
        );

        log.info("구독 완료 - phoneNumber: {}, status: {}", phoneNumber, newStatus);
        return SubscriptionResponse.of(phoneNumber, newStatus);
    }

    // 구독 해지
    @Transactional
    public SubscriptionResponse unsubscribe(String phoneNumber, Long channelId, SubscriptionStatus newStatus) {

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        if (channel.getType() == ChannelType.SUBSCRIBE) {
            throw new BusinessException(ErrorCode.CHANNEL_CANNOT_CANCEL);
        }

        Member member = memberRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Subscription subscription = subscriptionRepository.findByMember(member)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        validateUnSubscribeStatus(subscription.getStatus(), newStatus);
        subscription.changeStatus(newStatus);

        if (!csrngClient.isRollback()) {
            log.info("트랜잭션 롤백, phoneNumber: {}", phoneNumber);
            throw new BusinessException(ErrorCode.TRANSACTION_ROLLBACK);
        }

        subscriptionHistoryRepository.save(
                SubscriptionHistory.builder()
                        .member(member)
                        .channel(channel)
                        .actionType(ActionType.UNSUBSCRIBE)
                        .changedStatus(newStatus)
                        .build()
        );

        log.info("해지 완료 - phoneNumber: {}, status: {}", phoneNumber, newStatus);
        return SubscriptionResponse.of(phoneNumber, newStatus);
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

    // 구독 해지 검증
    private void validateUnSubscribeStatus(SubscriptionStatus current, SubscriptionStatus newStatus) {
        switch (current) {
            case PREMIUM -> {
                if (newStatus == SubscriptionStatus.PREMIUM) {
                    throw new BusinessException(ErrorCode.INVALID_SUBSCRIPTION_STATUS);
                }
            }
            case NORMAL -> {
                if (newStatus != SubscriptionStatus.NONE) {
                    throw new BusinessException(ErrorCode.INVALID_SUBSCRIPTION_STATUS);
                }
            }
            case NONE -> throw new BusinessException(ErrorCode.ALREADY_UNSUBSCRIBED);
        }
    }

    // 구독 변경 활동 상태
    private ActionType determineActionType(SubscriptionStatus previous, SubscriptionStatus newStatus) {
        if (previous == SubscriptionStatus.NORMAL && newStatus == SubscriptionStatus.PREMIUM) {
            return ActionType.UPGRADE;
        }
        return ActionType.SUBSCRIBE;
    }
}