package com.example.system.config;

import com.example.system.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务配置（预约过期检查、数据清理等）
 */
@Slf4j
@Component
@EnableScheduling
public class ScheduleConfig {

    @Autowired
    private ReservationService reservationService;

    /**
     * 每小时执行一次，自动将超时未支付的预约设为过期
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void autoExpireReservations() {
        log.info("开始执行自动过期预约任务");
        try {
            reservationService.autoExpireReservations();
            log.info("自动过期预约任务执行完成");
        } catch (Exception e) {
            log.error("自动过期预约任务执行失败", e);
        }
    }

    /**
     * 每天凌晨2点执行，清理过期数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredData() {
        log.info("开始执行清理过期数据任务");
        // 这里可以添加清理过期数据的逻辑
    }
}