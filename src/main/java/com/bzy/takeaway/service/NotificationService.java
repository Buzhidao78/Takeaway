package com.bzy.takeaway.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bzy.takeaway.entity.Notification;

import java.util.List;

public interface NotificationService {

    void sendNotification(Long userId, String title, String content, String type, Long relatedId);

    void sendNotification(Long userId, String title, String content, String type, Long relatedId, Integer auditStatus);

    Page<Notification> getNotificationList(Long userId, Integer pageNum, Integer pageSize, String type);

    long getUnreadCount(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    void deleteNotification(Long notificationId, Long userId);

    List<Notification> getRecentNotifications(Long userId, int limit);
}