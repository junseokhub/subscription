package com.api.subscription.subscription.service;

import com.api.subscription.common.exception.BusinessException;
import com.api.subscription.common.exception.ErrorCode;
import com.api.subscription.member.domain.Member;
import com.api.subscription.member.repository.MemberRepository;
import com.api.subscription.subscription.domain.SubscriptionHistory;
import com.api.subscription.subscription.dto.HistoryResponse;
import com.api.subscription.subscription.repository.SubscriptionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionHistoryService {

    private final MemberRepository memberRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final ChatClient chatClient;

    // 구독 기록 가져오기
    @Transactional(readOnly = true)
    public HistoryResponse getHistory(String phoneNumber) {
        Member member = memberRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        List<SubscriptionHistory> histories = subscriptionHistoryRepository
                .findByMemberWithChannel(member);

        if (histories.isEmpty()) {
            return HistoryResponse.of(histories, "구독 이력이 없습니다.");
        }

        String summary = generateSummary(histories);
        return HistoryResponse.of(histories, summary);
    }

    // LLM API (구독 이력 자연어 요약)
    private String generateSummary(List<SubscriptionHistory> histories) {
        StringBuilder historyText = new StringBuilder();
        histories.forEach(h -> historyText.append(String.format(
                "- %s | 채널: %s | 행위: %s | 상태: %s%n",
                h.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                h.getChannel().getName(),
                h.getActionType(),
                h.getChangedStatus()
        )));

        try {
            return chatClient.prompt()
                    .system("너는 구독 서비스 이력을 한국어로 요약해야해" +
                            "날짜, 채널, 구독 상태 변경을 포함하여 간결하게 한 문단으로 요약해"
                            )
                    .user("다음 구독 이력을 요약:\n" + historyText)
                    .call()
                    .content();
        } catch (Exception e) {
            log.warn("AI 요약 생성 실패: {}", e.getMessage());
            return "요약을 생성할 수 없습니다.";
        }
    }
}