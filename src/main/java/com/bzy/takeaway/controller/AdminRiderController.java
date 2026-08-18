package com.bzy.takeaway.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bzy.takeaway.common.Result;
import com.bzy.takeaway.entity.DeliveryOrder;
import com.bzy.takeaway.entity.Notification;
import com.bzy.takeaway.entity.Rider;
import com.bzy.takeaway.entity.RiderAuditHistory;
import com.bzy.takeaway.entity.RiderLocation;
import com.bzy.takeaway.entity.SysUser;
import com.bzy.takeaway.mapper.NotificationMapper;
import com.bzy.takeaway.mapper.RiderAuditHistoryMapper;
import com.bzy.takeaway.mapper.SysUserMapper;
import com.bzy.takeaway.service.NotificationService;
import com.bzy.takeaway.service.RiderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/admin/rider")
@RequiredArgsConstructor
@Slf4j
public class AdminRiderController {

    private final RiderService riderService;
    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;
    private final RiderAuditHistoryMapper riderAuditHistoryMapper;
    private final SysUserMapper sysUserMapper;

    @GetMapping("/list")
    public Result<Page<Rider>> list(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) Integer status) {
        Page<Rider> p = riderService.listRiders(page, size, keyword, status);
        return Result.ok(p);
    }

    @GetMapping("/audit-history")
    public Result<Page<RiderAuditHistory>> auditHistory(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(required = false) Long riderId) {
        var q = new LambdaQueryWrapper<RiderAuditHistory>()
            .orderByDesc(RiderAuditHistory::getAuditTime);
        if (riderId != null) {
            q.eq(RiderAuditHistory::getRiderId, riderId);
        }
        return Result.ok(riderAuditHistoryMapper.selectPage(new Page<>(page, size), q));
    }

    @PutMapping("/verify/{id}")
    public Result<Rider> verify(@PathVariable Long id, @RequestBody Map<String, Integer> body, HttpServletRequest request) {
        try {
            Integer auditStatus = body.get("auditStatus");
            String rejectReason = body.get("rejectReason") != null ? body.get("rejectReason").toString() : null;
            Rider r = riderService.verify(id, auditStatus);
            
            Long auditorId = (Long) request.getAttribute("userId");
            String auditorName = "管理员";
            if (auditorId != null) {
                SysUser auditor = sysUserMapper.selectById(auditorId);
                if (auditor != null) {
                    auditorName = auditor.getNickname();
                }
            }
            
            RiderAuditHistory history = new RiderAuditHistory();
            history.setRiderId(r.getId());
            history.setRiderName(r.getName());
            history.setRiderPhone(r.getPhone());
            history.setRiderIdCard(r.getIdCard());
            history.setAuditStatus(auditStatus);
            history.setRejectReason(rejectReason);
            history.setAuditTime(LocalDateTime.now());
            history.setAuditorId(auditorId);
            history.setAuditorName(auditorName);
            riderAuditHistoryMapper.insert(history);
            
            try {
                String title = auditStatus == 1 ? "骑手审核通过" : "骑手审核拒绝";
                String content = auditStatus == 1 
                    ? String.format("恭喜！您的骑手申请已通过审核，可以开始接单了", r.getName())
                    : String.format("很遗憾，您的骑手申请审核未通过", r.getName());
                notificationService.sendNotification(r.getUserId(), title, content, "audit", r.getId());
            } catch (Exception e) {
                log.warn("发送骑手审核结果通知失败: {}", e.getMessage());
            }
            
            try {
                notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                    .eq(Notification::getType, "audit")
                    .eq(Notification::getRelatedId, r.getId())
                    .eq(Notification::getAuditStatus, 0)
                    .set(Notification::getAuditStatus, auditStatus));
            } catch (Exception e) {
                log.warn("更新通知审核状态失败: {}", e.getMessage());
            }
            
            return Result.ok(r);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/available")
    public Result<List<Rider>> getAvailableRiders() {
        List<Rider> riders = riderService.getAvailableRiders();
        return Result.ok(riders);
    }

    @PostMapping("/assign")
    public Result<DeliveryOrder> assign(@RequestBody DeliveryOrder delivery) {
        try {
            DeliveryOrder d = riderService.assignRider(delivery.getOrderId(), delivery.getRiderId());
            return Result.ok(d);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/location/{riderId}")
    public Result<RiderLocation> getLocation(@PathVariable Long riderId) {
        RiderLocation location = riderService.getLocation(riderId);
        return Result.ok(location);
    }
}
