package com.bzy.takeaway.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bzy.takeaway.common.Constants;
import com.bzy.takeaway.entity.*;
import com.bzy.takeaway.mapper.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantService {

    private final StoreMapper storeMapper;
    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;
    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final DeliveryOrderMapper deliveryOrderMapper;
    private final GrabOrderService grabOrderService;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final NotificationService notificationService;
    private final StoreEarningsMapper storeEarningsMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Store getMyStore(Long userId) {
        Store store = storeMapper.selectOne(new LambdaQueryWrapper<Store>().eq(Store::getUserId, userId));
        if (store != null) {
            // 处理轮播图：将 JSON 字符串转换为 List
            if (store.getCarouselImages() != null && !store.getCarouselImages().isEmpty()) {
                try {
                    List<String> carouselImages = objectMapper.readValue(
                        store.getCarouselImages(), 
                        new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}
                    );
                    // 使用 @TableField(exist = false) 的临时字段存储
                    // 但由于已经去掉了该字段，我们需要在前端处理
                    // 这里我们直接修改 store 对象的 carouselImages 为 JSON 格式
                    // 前端会负责解析
                } catch (JsonProcessingException e) {
                    log.error("轮播图 JSON 解析失败", e);
                }
            }
        }
        return store;
    }

    public Store updateStore(Long userId, Store store) {
        Store s = getMyStore(userId);
        if (s == null) throw new RuntimeException("店铺不存在");
        
        store.setId(s.getId());
        store.setUserId(userId);
        storeMapper.updateById(store);
        return storeMapper.selectById(s.getId());
    }

    public List<Category> listCategories(Long userId) {
        Store s = getMyStore(userId);
        if (s == null) return List.of();
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>().eq(Category::getStoreId, s.getId()).orderByAsc(Category::getSort));
    }

    @Transactional
    public Category saveCategory(Long userId, Category cat) {
        Store s = getMyStore(userId);
        if (s == null) throw new RuntimeException("店铺不存在");
        cat.setStoreId(s.getId());
        if (cat.getId() == null) categoryMapper.insert(cat);
        else categoryMapper.updateById(cat);
        return cat;
    }

    public void deleteCategory(Long userId, Long id) {
        Store s = getMyStore(userId);
        if (s == null) return;
        categoryMapper.delete(new LambdaQueryWrapper<Category>().eq(Category::getId, id).eq(Category::getStoreId, s.getId()));
    }

    public List<Dish> listDishes(Long userId, Long categoryId) {
        Store s = getMyStore(userId);
        if (s == null) return List.of();
        var q = new LambdaQueryWrapper<Dish>().eq(Dish::getStoreId, s.getId());
        if (categoryId != null) q.eq(Dish::getCategoryId, categoryId);
        return dishMapper.selectList(q.orderByAsc(Dish::getSort));
    }

    @Transactional
    public Dish saveDish(Long userId, Dish dish) {
        Store s = getMyStore(userId);
        if (s == null) throw new RuntimeException("店铺不存在");
        
        dish.setStoreId(s.getId());
        
        dish.validate();
        
        if (dish.getId() == null) {
            dishMapper.insert(dish);
        } else {
            Dish existingDish = dishMapper.selectById(dish.getId());
            if (existingDish == null || !existingDish.getStoreId().equals(s.getId())) {
                throw new RuntimeException("菜品不存在或不属于该店铺");
            }
            dishMapper.updateById(dish);
        }
        return dish;
    }

    public void deleteDish(Long userId, Long id) {
        Store s = getMyStore(userId);
        if (s == null) return;
        dishMapper.delete(new LambdaQueryWrapper<Dish>().eq(Dish::getId, id).eq(Dish::getStoreId, s.getId()));
    }

    public Page<Orders> listOrders(Long userId, Integer status, int page, int size) {
        Store s = getMyStore(userId);
        if (s == null) return new Page<>(page, size);
        var q = new LambdaQueryWrapper<Orders>().eq(Orders::getStoreId, s.getId()).orderByDesc(Orders::getCreateTime);
        if (status != null) q.eq(Orders::getStatus, status);
        return ordersMapper.selectPage(new Page<>(page, size), q);
    }

    public Orders orderDetail(Long userId, Long orderId) {
        Store s = getMyStore(userId);
        if (s == null) return null;
        Orders o = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>().eq(Orders::getId, orderId).eq(Orders::getStoreId, s.getId()));
        if (o != null) {
            o.setItems(orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)));
        }
        return o;
    }

    @Transactional
    public void updateOrderStatus(Long userId, Long orderId, int status) {
        Store s = getMyStore(userId);
        if (s == null) return;
        Orders o = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>().eq(Orders::getId, orderId).eq(Orders::getStoreId, s.getId()));
        if (o != null) {
            int oldStatus = o.getStatus();
            o.setStatus(status);
            ordersMapper.updateById(o);

            logStatusLog(orderId, 1, oldStatus, status, userId, 2, "商家更新订单状态");

            // 商家接单 (status 1->2)
            if (status == 2 && oldStatus == 1) {
                try {
                    notificationService.sendNotification(o.getUserId(), "商家已接单", "商家已接单，正在制作中", "order", orderId);
                } catch (Exception e) {
                    log.warn("发送商家接单通知失败: {}", e.getMessage());
                }
            }
            
            // 商家拒单 (status 1->5)
            if (status == 5 && oldStatus == 1) {
                try {
                    notificationService.sendNotification(o.getUserId(), "订单被拒绝", "商家已拒绝您的订单，款项将原路退回", "order", orderId);
                } catch (Exception e) {
                    log.warn("发送商家拒单通知失败: {}", e.getMessage());
                }
            }

            // 出餐时（status 2->3）创建配送单并加入抢单池
            if (status == 3 && oldStatus == 2) {
                try {
                    notificationService.sendNotification(o.getUserId(), "商家已出餐", "商家已出餐，等待骑手取餐", "order", orderId);
                } catch (Exception e) {
                    log.warn("发送商家出餐通知失败: {}", e.getMessage());
                }
                
                // 检查是否已存在配送单
                DeliveryOrder existingDelivery = deliveryOrderMapper.selectOne(
                    new LambdaQueryWrapper<DeliveryOrder>().eq(DeliveryOrder::getOrderId, orderId));
                if (existingDelivery == null) {
                    DeliveryOrder deliveryOrder = new DeliveryOrder();
                    deliveryOrder.setOrderId(orderId);
                    deliveryOrder.setStatus(0);
                    deliveryOrder.setGrabStatus(0);
                    deliveryOrder.setExpireTime(LocalDateTime.now().plusMinutes(30));
                    
                    // 从订单中获取配送费
                    Orders order = ordersMapper.selectById(orderId);
                    if (order != null && order.getDeliveryFee() != null) {
                        deliveryOrder.setFee(order.getDeliveryFee());
                    }
                    
                    deliveryOrderMapper.insert(deliveryOrder);

                    grabOrderService.addToGrabPool(deliveryOrder.getId());

                    logStatusLog(deliveryOrder.getId(), 2, null, 0, userId, 2, "商家出餐，创建配送单并加入抢单池");
                }
            }
        }
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

    @Transactional
    public Store updateStoreStatus(Long userId, Integer status) {
        Store s = getMyStore(userId);
        if (s == null) throw new RuntimeException("店铺不存在");
        if (status < 0 || status > 2) throw new RuntimeException("状态参数错误");
        
        s.setStatus(status);
        storeMapper.updateById(s);
        return storeMapper.selectById(s.getId());
    }

    public Map<String, Object> getDashboardData(Long userId) {
        Store s = getMyStore(userId);
        if (s == null) return Map.of();
        
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        var q = new LambdaQueryWrapper<Orders>()
            .eq(Orders::getStoreId, s.getId())
            .eq(Orders::getStatus, 4)
            .ge(Orders::getCreateTime, thirtyDaysAgo);
        
        long monthSales = ordersMapper.selectCount(q);
        
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);

        BigDecimal todayEarnings = storeEarningsMapper.selectList(new LambdaQueryWrapper<StoreEarnings>()
                .eq(StoreEarnings::getStoreId, s.getId())
                .eq(StoreEarnings::getType, 1)
                .ge(StoreEarnings::getCreateTime, startOfDay)
                .le(StoreEarnings::getCreateTime, endOfDay))
                .stream()
                .map(StoreEarnings::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEarnings = storeEarningsMapper.selectList(new LambdaQueryWrapper<StoreEarnings>()
                .eq(StoreEarnings::getStoreId, s.getId())
                .eq(StoreEarnings::getType, 1))
                .stream()
                .map(StoreEarnings::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = ordersMapper.selectCount(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStoreId, s.getId())
                .eq(Orders::getStatus, 4));
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("monthSales", monthSales);
        result.put("monthOrders", monthSales);
        result.put("todayEarnings", todayEarnings);
        result.put("totalEarnings", totalEarnings);
        result.put("totalOrders", totalOrders);
        result.put("balance", s.getBalance() != null ? s.getBalance() : BigDecimal.ZERO);
        
        return result;
    }

    public Map<String, Object> getEarningsStats(Long userId) {
        Store s = getMyStore(userId);
        if (s == null) return Map.of();

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);

        BigDecimal todayEarnings = storeEarningsMapper.selectList(new LambdaQueryWrapper<StoreEarnings>()
                .eq(StoreEarnings::getStoreId, s.getId())
                .eq(StoreEarnings::getType, 1)
                .ge(StoreEarnings::getCreateTime, startOfDay)
                .le(StoreEarnings::getCreateTime, endOfDay))
                .stream()
                .map(StoreEarnings::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEarnings = storeEarningsMapper.selectList(new LambdaQueryWrapper<StoreEarnings>()
                .eq(StoreEarnings::getStoreId, s.getId())
                .eq(StoreEarnings::getType, 1))
                .stream()
                .map(StoreEarnings::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = ordersMapper.selectCount(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStoreId, s.getId())
                .eq(Orders::getStatus, 4));

        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("todayEarnings", todayEarnings);
        stats.put("totalEarnings", totalEarnings);
        stats.put("totalOrders", totalOrders);
        stats.put("balance", s.getBalance() != null ? s.getBalance() : BigDecimal.ZERO);

        return stats;
    }

    public Page<StoreEarnings> getEarnings(Long userId, int page, int size) {
        Store s = getMyStore(userId);
        if (s == null) return new Page<>(page, size);
        Page<StoreEarnings> pageResult = storeEarningsMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<StoreEarnings>()
                        .eq(StoreEarnings::getStoreId, s.getId())
                        .orderByDesc(StoreEarnings::getCreateTime));
        // 为每条收益记录设置订单号
        for (StoreEarnings earning : pageResult.getRecords()) {
            if (earning.getOrderId() != null) {
                Orders order = ordersMapper.selectById(earning.getOrderId());
                if (order != null) {
                    earning.setOrderNo(order.getOrderNo());
                }
            }
        }
        return pageResult;
    }
}
