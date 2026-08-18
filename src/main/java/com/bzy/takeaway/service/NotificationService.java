package com.bzy.takeaway.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bzy.takeaway.entity.Notification;
import com.bzy.takeaway.mapper.NotificationMapper;
import com.bzy.takeaway.websocket.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final NotificationWebSocketHandler notificationWebSocketHandler;

    @Async("notificationExecutor")
    public void sendNotification(Long userId, String title, String content, String type, Long relatedId) {
        sendNotification(userId, title, content, type, relatedId, 0);
    }

    @Async("notificationExecutor")
    public void sendNotification(Long userId, String title, String content, String type, Long relatedId, Integer auditStatus) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        notification.setAuditStatus(auditStatus);
        notificationMapper.insert(notification);
        log.info("发送通知给用户 {}：{}", userId, title);

        // 实时推送通知（失败不影响数据库存储）
        try {
            notificationWebSocketHandler.sendNotification(userId, Map.of(
                    "id", notification.getId(),
                    "title", title,
                    "content", content,
                    "type", type,
                    "relatedId", relatedId,
                    "isRead", 0,
                    "auditStatus", auditStatus,
                    "createTime", notification.getCreateTime()
            ));
        } catch (Exception e) {
            log.warn("WebSocket 推送通知失败: {}", e.getMessage());
        }
    }

    public Page<Notification> getNotificationList(Long userId, Integer pageNum, Integer pageSize, String type) {
        Page<Notification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Notification::getType, type);
        }
        wrapper.orderByDesc(Notification::getCreateTime);
        return notificationMapper.selectPage(page, wrapper);
    }

    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0);
        return notificationMapper.selectCount(wrapper);
    }

    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.setIsRead(1);
            notificationMapper.updateById(notification);
        }
    }

    public void markAllAsRead(Long userId) {
        Notification notification = new Notification();
        notification.setIsRead(1);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0);
        notificationMapper.update(notification, wrapper);
    }

    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification != null && notification.getUserId().equals(userId)) {
            notificationMapper.deleteById(notificationId);
        }
    }

    public List<Notification> getRecentNotifications(Long userId, int limit) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .orderByDesc(Notification::getCreateTime)
               .last("LIMIT " + limit);
        return notificationMapper.selectList(wrapper);
    }
}
