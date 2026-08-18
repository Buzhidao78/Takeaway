package com.bzy.takeaway.service;

import com.bzy.takeaway.entity.DeliveryOrder;
import com.bzy.takeaway.entity.OrderStatusLog;
import com.bzy.takeaway.entity.Orders;
import com.bzy.takeaway.mapper.DeliveryOrderMapper;
import com.bzy.takeaway.mapper.OrderStatusLogMapper;
import com.bzy.takeaway.mapper.OrdersMapper;
import com.bzy.takeaway.mapper.RiderMapper;
import com.bzy.takeaway.mapper.RiderOnlineMapper;
import com.bzy.takeaway.websocket.RiderWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrabOrderService {

    private final StringRedisTemplate redisTemplate;
    private final DeliveryOrderMapper deliveryOrderMapper;
    private final OrdersMapper ordersMapper;
    private final RiderMapper riderMapper;
    private final RiderOnlineMapper riderOnlineMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final NotificationService notificationService;
    
    private RiderWebSocketHandler riderWebSocketHandler;
    
    public void setRiderWebSocketHandler(RiderWebSocketHandler riderWebSocketHandler) {
        this.riderWebSocketHandler = riderWebSocketHandler;
    }

    private static final String GRAB_POOL_KEY = "grab:pool";
    private static final String GRAB_LOCK_KEY = "grab:lock:";
    private static final String RIDER_ONLINE_KEY = "rider:online:set";
    private static final long GRAB_EXPIRE_MINUTES = 30;

    public void addToGrabPool(Long deliveryOrderId) {
        DeliveryOrder order = deliveryOrderMapper.selectById(deliveryOrderId);
        if (order == null) {
            log.warn("配送单不存在: {}", deliveryOrderId);
            return;
        }

        order.setGrabStatus(0);
        order.setExpireTime(LocalDateTime.now().plusMinutes(GRAB_EXPIRE_MINUTES));
        deliveryOrderMapper.updateById(order);

        redisTemplate.opsForSet().add(GRAB_POOL_KEY, String.valueOf(deliveryOrderId));

        Set<String> onlineRiders = getOnlineRiders();
        if (onlineRiders != null && !onlineRiders.isEmpty() && riderWebSocketHandler != null) {
            for (String riderIdStr : onlineRiders) {
                Long riderId = Long.valueOf(riderIdStr);
                riderWebSocketHandler.sendNewOrderNotification(riderId, Map.of(
                        "deliveryOrderId", order.getId(),
                        "orderId", order.getOrderId(),
                        "distance", order.getDistance(),
                        "fee", order.getFee(),
                        "expireTime", order.getExpireTime()
                ));
            }
            log.info("已向 {} 个在线骑手推送新订单", onlineRiders.size());
        }

        log.info("配送单 {} 已加入抢单池", deliveryOrderId);
    }

    public boolean grabOrder(Long deliveryOrderId, Long riderId) {
        String lockKey = GRAB_LOCK_KEY + deliveryOrderId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, String.valueOf(riderId), 10, TimeUnit.SECONDS);
        if (locked == null || !locked) {
            log.warn("抢单失败，订单 {} 正在被其他骑手抢单", deliveryOrderId);
            return false;
        }

        try {
            DeliveryOrder order = deliveryOrderMapper.selectById(deliveryOrderId);
            if (order == null) {
                log.warn("抢单失败，配送单不存在: {}", deliveryOrderId);
                return false;
            }

            if (order.getGrabStatus() != 0) {
                log.warn("抢单失败，配送单 {} 已被抢", deliveryOrderId);
                return false;
            }

            if (order.getExpireTime() != null && order.getExpireTime().isBefore(LocalDateTime.now())) {
                log.warn("抢单失败，配送单 {} 已过期", deliveryOrderId);
                redisTemplate.opsForSet().remove(GRAB_POOL_KEY, String.valueOf(deliveryOrderId));
                return false;
            }

            // 检查骑手是否在线
            if (!isRiderOnline(riderId)) {
                log.warn("抢单失败，骑手 {} 处于离线状态", riderId);
                throw new RuntimeException("请先上线后再抢单");
            }

            order.setGrabStatus(1);
            order.setRiderId(riderId);
            order.setGrabTime(LocalDateTime.now());
            order.setStatus(1);
            deliveryOrderMapper.updateById(order);

            redisTemplate.opsForSet().remove(GRAB_POOL_KEY, String.valueOf(deliveryOrderId));

            Orders mainOrder = ordersMapper.selectById(order.getOrderId());
            if (mainOrder != null) {
                mainOrder.setStatus(3);
                ordersMapper.updateById(mainOrder);
                logStatusLog(mainOrder.getId(), 1, 2, 3, riderId, 3, "骑手抢单成功");
            }

            logStatusLog(order.getId(), 2, 0, 1, riderId, 3, "骑手抢单成功");

            // 发送通知给用户
            if (mainOrder != null) {
                notificationService.sendNotification(mainOrder.getUserId(), "骑手已接单", "骑手已接单，正在前往商家取餐", "order", mainOrder.getId());
            }

            log.info("骑手 {} 抢单成功，配送单: {}", riderId, deliveryOrderId);
            return true;

        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    public List<DeliveryOrder> getGrabList() {
        Set<String> orderIds = redisTemplate.opsForSet().members(GRAB_POOL_KEY);
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> ids = orderIds.stream().map(Long::valueOf).collect(Collectors.toList());
        List<DeliveryOrder> orders = deliveryOrderMapper.selectBatchIds(ids);

        return orders.stream()
                .filter(o -> o.getGrabStatus() == 0)
                .filter(o -> o.getExpireTime() == null || o.getExpireTime().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
    }

    public void removeExpiredOrders() {
        Set<String> orderIds = redisTemplate.opsForSet().members(GRAB_POOL_KEY);
        if (orderIds == null || orderIds.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (String orderIdStr : orderIds) {
            Long orderId = Long.valueOf(orderIdStr);
            DeliveryOrder order = deliveryOrderMapper.selectById(orderId);
            if (order != null && order.getExpireTime() != null && order.getExpireTime().isBefore(now)) {
                order.setGrabStatus(2);
                deliveryOrderMapper.updateById(order);
                redisTemplate.opsForSet().remove(GRAB_POOL_KEY, orderIdStr);
                log.info("配送单 {} 抢单超时，已移除", orderId);
            }
        }
    }

    public void setRiderOnline(Long riderId, boolean online) {
        if (online) {
            redisTemplate.opsForSet().add(RIDER_ONLINE_KEY, String.valueOf(riderId));
        } else {
            redisTemplate.opsForSet().remove(RIDER_ONLINE_KEY, String.valueOf(riderId));
        }
    }

    public boolean isRiderOnline(Long riderId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(RIDER_ONLINE_KEY, String.valueOf(riderId)));
    }

    public Set<String> getOnlineRiders() {
        return redisTemplate.opsForSet().members(RIDER_ONLINE_KEY);
    }

    private void logStatusLog(Long orderId, Integer orderType, Integer oldStatus, Integer newStatus,
                              Long operatorId, Integer operatorType, String remark) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(orderId);
        log.setOrderType(orderType);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setOperatorId(operatorId);
        log.setOperatorType(operatorType);
        log.setRemark(remark);
        orderStatusLogMapper.insert(log);
    }
}
