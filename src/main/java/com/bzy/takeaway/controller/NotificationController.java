package com.bzy.takeaway.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bzy.takeaway.common.Result;
import com.bzy.takeaway.entity.Notification;
import com.bzy.takeaway.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/list")
    public Result<Page<Notification>> getNotificationList(
            @RequestAttribute Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type) {
        Page<Notification> page = notificationService.getNotificationList(userId, pageNum, pageSize, type);
        return Result.ok(page);
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Long>> getUnreadCount(@RequestAttribute Long userId) {
        long count = notificationService.getUnreadCount(userId);
        Map<String, Long> result = new HashMap<>();
        result.put("count", count);
        return Result.ok(result);
    }

    @PostMapping("/{id}/read")
    public Result<Void> markAsRead(@RequestAttribute Long userId, @PathVariable Long id) {
        notificationService.markAsRead(id, userId);
        return Result.ok();
    }

    @PostMapping("/read-all")
    public Result<Void> markAllAsRead(@RequestAttribute Long userId) {
        notificationService.markAllAsRead(userId);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(@RequestAttribute Long userId, @PathVariable Long id) {
        notificationService.deleteNotification(id, userId);
        return Result.ok();
    }

    @GetMapping("/recent")
    public Result<List<Notification>> getRecentNotifications(
            @RequestAttribute Long userId,
            @RequestParam(defaultValue = "5") Integer limit) {
        List<Notification> notifications = notificationService.getRecentNotifications(userId, limit);
        return Result.ok(notifications);
    }
}
