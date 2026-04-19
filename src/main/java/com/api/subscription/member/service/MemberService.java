package com.api.subscription.member.service;

import com.api.subscription.common.exception.BusinessException;
import com.api.subscription.common.exception.ErrorCode;
import com.api.subscription.member.domain.Member;
import com.api.subscription.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Member findMemberByPhoneNumber(String phoneNumber) {
        return memberRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional
    public Member getOrCreateMember(String phoneNumber) {
        return memberRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .phoneNumber(phoneNumber)
                                .build()
                ));
    }
}