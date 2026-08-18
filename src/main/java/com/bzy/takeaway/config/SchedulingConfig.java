package com.bzy.takeaway.config;

import com.bzy.takeaway.mapper.NotificationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bzy.takeaway.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulingConfig {

    private final NotificationMapper notificationMapper;

    /**
     * 每天凌晨 2 点清理 30 天前的已读通知
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanOldNotifications() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getIsRead, 1)
               .lt(Notification::getCreateTime, threshold);
        
        int deletedCount = notificationMapper.delete(wrapper);
        log.info("清理过期通知完成，共删除 {} 条", deletedCount);
    }
}
