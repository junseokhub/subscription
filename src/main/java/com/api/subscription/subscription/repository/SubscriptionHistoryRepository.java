package com.api.subscription.subscription.repository;

import com.api.subscription.member.domain.Member;
import com.api.subscription.subscription.domain.SubscriptionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {

    @Query("SELECT h FROM SubscriptionHistory h " +
            "JOIN FETCH h.channel " +
            "WHERE h.member = :member " +
            "ORDER BY h.createdAt DESC")
    // @EntityGraph(attributePaths = "channel") 현재 구조에서는 사실 이거로도 충분할 것 같습니다.
    // 추후 복잡학 동적 쿼리 필요 시 QueryDSL 도입 검토 해봐야 할 것으로 생각됩니다.
    List<SubscriptionHistory> findByMemberWithChannel(@Param("member") Member member);
}
