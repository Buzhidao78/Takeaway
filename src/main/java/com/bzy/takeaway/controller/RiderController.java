package com.bzy.takeaway.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bzy.takeaway.common.Result;
import com.bzy.takeaway.entity.DeliveryOrder;
import com.bzy.takeaway.entity.Rider;
import com.bzy.takeaway.entity.RiderEarnings;
import com.bzy.takeaway.entity.RiderLocation;
import com.bzy.takeaway.entity.RiderOnline;
import com.bzy.takeaway.service.GrabOrderService;
import com.bzy.takeaway.service.RiderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rider")
@RequiredArgsConstructor
public class RiderController {

    private final RiderService riderService;
    private final GrabOrderService grabOrderService;

    @PostMapping("/register")
    public Result<Rider> register(@RequestBody Rider rider) {
        try {
            Rider r = riderService.register(rider);
            return Result.ok(r);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<Rider> login(@RequestBody Map<String, String> body) {
        try {
            Rider r = riderService.login(body.get("phone"), body.get("password"));
            return Result.ok(r);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/profile")
    public Result<Rider> profile(@RequestAttribute("riderId") Long riderId) {
        try {
            Rider r = riderService.getProfile(riderId);
            if (r != null) {
                return Result.ok(r);
            }
            return Result.fail("骑手不存在");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PutMapping("/profile")
    public Result<Rider> updateProfile(@RequestAttribute("riderId") Long riderId, @RequestBody Rider rider) {
        try {
            Rider r = riderService.updateProfile(riderId, rider);
            return Result.ok(r);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PutMapping("/status")
    public Result<Rider> updateStatus(@RequestAttribute("riderId") Long riderId, @RequestBody Map<String, Integer> body) {
        try {
            Rider r = riderService.updateStatus(riderId, body.get("status"));
            return Result.ok(r);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/location")
    public Result<Void> updateLocation(@RequestAttribute("riderId") Long riderId, @RequestBody RiderLocation location) {
        try {
            riderService.updateLocation(riderId, location);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/location/{riderId}")
    public Result<RiderLocation> getLocation(@PathVariable Long riderId) {
        RiderLocation location = riderService.getLocation(riderId);
        return Result.ok(location);
    }

    @GetMapping("/orders")
    public Result<List<DeliveryOrder>> getOrders(@RequestAttribute("riderId") Long riderId,
                                                  @RequestParam(required = false) Integer status) {
        List<DeliveryOrder> orders = riderService.getOrders(riderId, status);
        return Result.ok(orders);
    }

    @GetMapping("/orders/available")
    public Result<List<DeliveryOrder>> getAvailableOrders() {
        List<DeliveryOrder> orders = riderService.getAvailableOrders();
        return Result.ok(orders);
    }

    @PostMapping("/accept/{id}")
    public Result<DeliveryOrder> acceptOrder(@RequestAttribute("riderId") Long riderId, @PathVariable Long id) {
        try {
            DeliveryOrder order = riderService.acceptOrder(riderId, id);
            return Result.ok(order);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/pickup/{id}")
    public Result<DeliveryOrder> pickUp(@RequestAttribute("riderId") Long riderId, @PathVariable Long id) {
        try {
            DeliveryOrder order = riderService.pickUp(riderId, id);
            return Result.ok(order);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/complete/{id}")
    public Result<DeliveryOrder> complete(@RequestAttribute("riderId") Long riderId, @PathVariable Long id) {
        try {
            DeliveryOrder order = riderService.complete(riderId, id);
            return Result.ok(order);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/earnings")
    public Result<Map<String, Object>> getEarnings(
            @RequestAttribute("riderId") Long riderId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = new HashMap<String, Object>();
        var pageData = riderService.getEarnings(riderId, page, size);
        result.put("list", pageData.getRecords());
        result.put("total", pageData.getTotal());
        return Result.ok(result);
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> getRiderInfo(@RequestAttribute Long userId) {
        Rider rider = riderService.getRiderInfo(userId);
        if (rider == null) {
            return Result.fail("不是骑手用户");
        }
        Map<String, Object> info = new HashMap<>();
        info.put("riderId", rider.getId());
        info.put("name", rider.getName());
        info.put("phone", rider.getPhone());
        info.put("avatar", rider.getAvatar());
        info.put("status", rider.getStatus());
        info.put("auditStatus", rider.getAuditStatus());
        info.put("totalOrders", rider.getTotalOrders());
        info.put("rating", rider.getRating());
        info.put("balance", rider.getBalance());
        
        // 获取在线状态
        RiderOnline riderOnline = riderService.getRiderOnline(rider.getId());
        info.put("onlineStatus", riderOnline != null && riderOnline.getIsOnline() == 1);
        
        return Result.ok(info);
    }

    @PostMapping("/online")
    public Result<Void> toggleOnline(@RequestAttribute Long userId, @RequestBody Map<String, Boolean> body) {
        Rider rider = riderService.getRiderInfo(userId);
        if (rider == null) {
            return Result.fail("不是骑手用户");
        }
        try {
            riderService.toggleOnline(rider.getId(), body.get("online"));
            grabOrderService.setRiderOnline(rider.getId(), body.get("online"));
            return Result.ok();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/grab/list")
    public Result<List<DeliveryOrder>> getGrabList() {
        return Result.ok(grabOrderService.getGrabList());
    }

    @PostMapping("/grab/{orderId}")
    public Result<Void> grabOrder(@RequestAttribute Long userId, @PathVariable Long orderId) {
        Rider rider = riderService.getRiderInfo(userId);
        if (rider == null) {
            return Result.fail("不是骑手用户");
        }
        try {
            boolean success = grabOrderService.grabOrder(orderId, rider.getId());
            if (success) {
                return Result.ok();
            }
            return Result.fail("抢单失败");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/delivery/list")
    public Result<List<DeliveryOrder>> getDeliveryList(@RequestAttribute Long userId,
                                                        @RequestParam(required = false) String status) {
        Rider rider = riderService.getRiderInfo(userId);
        if (rider == null) {
            return Result.fail("不是骑手用户");
        }
        if ("delivering".equals(status)) {
            return Result.ok(riderService.getMyDeliveryOrders(rider.getId(), null, 1, 2));
        }
        Integer statusInt = status != null ? Integer.valueOf(status) : null;
        return Result.ok(riderService.getMyDeliveryOrders(rider.getId(), statusInt));
    }

    @PostMapping("/delivery/{id}/pickup")
    public Result<Void> pickup(@RequestAttribute Long userId, @PathVariable Long id) {
        Rider rider = riderService.getRiderInfo(userId);
        if (rider == null) {
            return Result.fail("不是骑手用户");
        }
        DeliveryOrder order = riderService.pickUp(rider.getId(), id);
        if (order != null) {
            return Result.ok();
        }
        return Result.fail("操作失败");
    }

    @PostMapping("/delivery/{id}/deliver")
    public Result<Void> deliver(@RequestAttribute Long userId, @PathVariable Long id) {
        Rider rider = riderService.getRiderInfo(userId);
        if (rider == null) {
            return Result.fail("不是骑手用户");
        }
        DeliveryOrder order = riderService.complete(rider.getId(), id);
        if (order != null) {
            return Result.ok();
        }
        return Result.fail("操作失败");
    }

    @GetMapping("/earnings/stats")
    public Result<Map<String, Object>> getEarningsStats(@RequestAttribute Long userId) {
        Rider rider = riderService.getRiderInfo(userId);
        if (rider == null) {
            return Result.fail("不是骑手用户");
        }
        return Result.ok(riderService.getTodayStats(rider.getId()));
    }

    @PostMapping("/heartbeat")
    public Result<Void> heartbeat(@RequestAttribute Long userId) {
        Rider rider = riderService.getRiderInfo(userId);
        if (rider == null) {
            return Result.fail("不是骑手用户");
        }
        riderService.heartbeat(rider.getId());
        return Result.ok();
    }
}
