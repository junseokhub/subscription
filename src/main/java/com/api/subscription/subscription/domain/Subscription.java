package com.api.subscription.subscription.domain;

import com.api.subscription.common.entity.BaseEntity;
import com.api.subscription.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SubscriptionStatus status;

    @Builder
    private Subscription(Member member, SubscriptionStatus status) {
        this.member = member;
        this.status = status;
    }

    public void changeStatus(SubscriptionStatus newStatus) {
        this.status = newStatus;
    }
}