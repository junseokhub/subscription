package com.api.subscription.subscription.repository;

import com.api.subscription.member.domain.Member;
import com.api.subscription.subscription.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByMember(Member member);
}
