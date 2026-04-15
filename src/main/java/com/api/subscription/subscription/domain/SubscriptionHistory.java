package com.api.subscription.subscription.domain;

import com.api.subscription.channel.domain.Channel;
import com.api.subscription.common.entity.BaseEntity;
import com.api.subscription.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subscription_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SubscriptionStatus changedStatus;

    @Builder
    private SubscriptionHistory(Member member, Channel channel,
                                ActionType actionType, SubscriptionStatus changedStatus) {
        this.member = member;
        this.channel = channel;
        this.actionType = actionType;
        this.changedStatus = changedStatus;
    }
}