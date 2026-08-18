package com.bzy.takeaway.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bzy.takeaway.common.Result;
import com.bzy.takeaway.entity.DeliveryOrder;
import com.bzy.takeaway.entity.OrderStatusLog;
import com.bzy.takeaway.entity.Orders;
import com.bzy.takeaway.entity.Rider;
import com.bzy.takeaway.mapper.DeliveryOrderMapper;
import com.bzy.takeaway.mapper.OrderStatusLogMapper;
import com.bzy.takeaway.mapper.RiderMapper;
import com.bzy.takeaway.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final DeliveryOrderMapper deliveryOrderMapper;
    private final RiderMapper riderMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;

    @PostMapping("/create")
    public Result<Orders> create(@RequestAttribute Long userId, @RequestBody Map<String, Object> body) {
        Long storeId = Long.valueOf(body.get("storeId").toString());
        Long addressId = Long.valueOf(body.get("addressId").toString());
        String remark = body.containsKey("remark") ? body.get("remark").toString() : null;
        
        // 获取包装费和配送费
        java.math.BigDecimal packagingFee = null;
        if (body.containsKey("packagingFee")) {
            packagingFee = new java.math.BigDecimal(body.get("packagingFee").toString());
        }
        
        java.math.BigDecimal deliveryFee = null;
        if (body.containsKey("deliveryFee")) {
            deliveryFee = new java.math.BigDecimal(body.get("deliveryFee").toString());
        }
        
        return Result.ok(orderService.create(userId, storeId, addressId, remark, packagingFee, deliveryFee));
    }

    @GetMapping("/my")
    public Result<Page<Orders>> myOrders(@RequestAttribute Long userId,
                                         @RequestParam(required = false) Integer status,
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return Result.ok(orderService.myOrders(userId, status, page, size));
    }

    @GetMapping("/{orderId}")
    public Result<Map<String, Object>> detail(@RequestAttribute Long userId, @PathVariable Long orderId) {
        Orders order = orderService.detail(userId, orderId);
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);

        DeliveryOrder deliveryOrder = deliveryOrderMapper.selectOne(new LambdaQueryWrapper<DeliveryOrder>()
                .eq(DeliveryOrder::getOrderId, orderId));
        if (deliveryOrder != null && deliveryOrder.getRiderId() != null) {
            Rider rider = riderMapper.selectById(deliveryOrder.getRiderId());
            result.put("rider", rider);
        }

        return Result.ok(result);
    }

    @GetMapping("/{orderId}/status-log")
    public Result<List<OrderStatusLog>> getStatusLog(@RequestAttribute Long userId, @PathVariable Long orderId) {
        List<OrderStatusLog> logs = orderStatusLogMapper.selectList(new LambdaQueryWrapper<OrderStatusLog>()
                .eq(OrderStatusLog::getOrderId, orderId)
                .orderByDesc(OrderStatusLog::getCreateTime));
        return Result.ok(logs);
    }

    @PostMapping("/{orderId}/pay")
    public Result<String> pay(@RequestAttribute Long userId, @PathVariable Long orderId) {
        return Result.ok(orderService.createPayForm(userId, orderId));
    }

    @PostMapping("/{orderId}/pay/success")
    public Result<?> paySuccess(@RequestAttribute Long userId, @PathVariable Long orderId) {
        orderService.paySuccess(userId, orderId);
        return Result.ok();
    }

    @GetMapping("/query-pay/{orderNo}")
    public Result<Map<String, Object>> queryPayStatus(@RequestAttribute Long userId, @PathVariable String orderNo) {
        boolean paid = orderService.queryPayStatus(userId, orderNo);
        Map<String, Object> result = new HashMap<>();
        result.put("paid", paid);
        return Result.ok(result);
    }

    @PostMapping("/{orderId}/cancel")
    public Result<?> cancel(@RequestAttribute Long userId, @PathVariable Long orderId) {
        orderService.cancel(userId, orderId);
        return Result.ok();
    }

    @PostMapping("/{orderId}/refund")
    public Result<?> refund(@RequestAttribute Long userId, @PathVariable Long orderId) {
        orderService.refund(userId, orderId);
        return Result.ok();
    }
}
