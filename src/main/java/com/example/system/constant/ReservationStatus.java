package com.example.system.constant;

/**
 * 预约状态常量
 */
public class ReservationStatus {
    public static final int PENDING_PAYMENT = 0;  // 待支付
    public static final int PAID = 1;              // 已支付
    public static final int USED = 2;              // 已使用
    public static final int CANCELLED = 3;         // 已取消
    public static final int EXPIRED = 4;           // 已过期
}