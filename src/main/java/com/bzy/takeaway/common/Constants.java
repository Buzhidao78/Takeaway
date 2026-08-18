package com.bzy.takeaway.common;

/**
 * 系统常量
 */
public interface Constants {
    String TOKEN_HEADER = "Authorization";
    String TOKEN_PREFIX = "Bearer ";
    String REDIS_SMS_PREFIX = "sms:code:";
    int SMS_EXPIRE_MINUTES = 5;
    int SMS_CODE_LENGTH = 6;

    int ROLE_USER = 0;
    int ROLE_MERCHANT = 1;
    int ROLE_ADMIN = 2;
    int ROLE_RIDER = 3;

    int ORDER_STATUS_UNPAID = 0;
    int ORDER_STATUS_PAID = 1;
    int ORDER_STATUS_MAKING = 2;
    int ORDER_STATUS_DELIVERING = 3;
    int ORDER_STATUS_COMPLETED = 4;
    int ORDER_STATUS_CANCELLED = 5;
    int ORDER_STATUS_REFUNDED = 6;

    int STORE_STATUS_CLOSED = 0;
    int STORE_STATUS_OPEN = 1;
    int STORE_STATUS_REST = 2;
}
