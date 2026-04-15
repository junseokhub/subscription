package com.api.subscription.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),

    CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 채널입니다."),
    CHANNEL_CANNOT_SUBSCRIBE(HttpStatus.BAD_REQUEST, "구독이 불가능한 채널입니다."),
    CHANNEL_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "해지가 불가능한 채널입니다."),

    INVALID_SUBSCRIPTION_STATUS(HttpStatus.BAD_REQUEST, "현재 상태에서 변경 불가능한 구독 상태입니다."),
    ALREADY_PREMIUM(HttpStatus.BAD_REQUEST, "이미 프리미엄 구독 상태입니다."),
    ALREADY_UNSUBSCRIBED(HttpStatus.BAD_REQUEST, "이미 구독하지 않은 상태입니다."),

    EXTERNAL_API_FAILURE(HttpStatus.SERVICE_UNAVAILABLE, "외부 API 호출에 실패했습니다."),
    TRANSACTION_ROLLBACK(HttpStatus.INTERNAL_SERVER_ERROR, "외부 API 응답에 의해 트랜잭션이 롤백되었습니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}