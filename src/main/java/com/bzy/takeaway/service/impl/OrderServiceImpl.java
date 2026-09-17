package com.bzy.takeaway.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bzy.takeaway.common.Constants;
import com.bzy.takeaway.entity.*;
import com.bzy.takeaway.mapper.*;
import com.bzy.takeaway.service.AlipayService;
import com.bzy.takeaway.service.NotificationService;
import com.bzy.takeaway.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final DishMapper dishMapper;
    private final UserAddressMapper addressMapper;
    private final StoreMapper storeMapper;
    private final ProductReviewMapper productReviewMapper;
    private final NotificationService notificationService;
    private final AlipayService alipayService;
    private final StoreEarningsMapper storeEarningsMapper;

    @Value("${alipay.enabled:false}")
    private boolean alipayEnabled;

    @Transactional
    @Override
    public Orders create(Long userId, Long storeId, Long addressId, String remark, BigDecimal packagingFee, BigDecimal deliveryFee) {
        UserAddress addr = addressMapper.selectOne(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getId, addressId).eq(UserAddress::getUserId, userId));
        if (addr == null) throw new RuntimeException("地址不存在");

        List<Cart> carts = cartMapper.selectList(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId).eq(Cart::getStoreId, storeId));
        if (carts.isEmpty()) throw new RuntimeException("购物车为空");

        // 计算商品总金额
        BigDecimal goodsAmount = BigDecimal.ZERO;
        for (Cart c : carts) {
            Dish d = dishMapper.selectById(c.getDishId());
            if (d == null || d.getStatus() != 1) throw new RuntimeException("菜品已下架：" + (d != null ? d.getName() : ""));
            BigDecimal amt = d.getPrice().multiply(BigDecimal.valueOf(c.getQuantity()));
            goodsAmount = goodsAmount.add(amt);
        }

        // 如果包装费和配送费为 null，设置默认值
        if (packagingFee == null) {
            packagingFee = BigDecimal.ONE; // 默认 1 元包装费
        }
        if (deliveryFee == null) {
            deliveryFee = BigDecimal.ZERO; // 默认 0 元配送费
        }

        // 计算总金额和实付金额
        BigDecimal totalAmount = goodsAmount.add(packagingFee).add(deliveryFee);
        BigDecimal payAmount = totalAmount; // 暂无优惠系统

        // 创建订单对象，设置所有必填字段
        Orders order = new Orders();
        order.setOrderNo("BZY" + System.currentTimeMillis() + (int)(Math.random()*1000));
        order.setUserId(userId);
        order.setStoreId(storeId);
        order.setAddressId(addressId);
        order.setStatus(Constants.ORDER_STATUS_UNPAID);
        order.setGoodsAmount(goodsAmount);
        order.setPackagingFee(packagingFee);
        order.setDeliveryFee(deliveryFee);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(payAmount);
        order.setRemark(remark);
        ordersMapper.insert(order);

        // 订单项插入
        for (Cart c : carts) {
            Dish d = dishMapper.selectById(c.getDishId());
            if (d == null || d.getStatus() != 1) throw new RuntimeException("菜品已下架：" + (d != null ? d.getName() : ""));
            BigDecimal amt = d.getPrice().multiply(BigDecimal.valueOf(c.getQuantity()));
            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setDishId(d.getId());
            oi.setDishName(d.getName());
            // 优先使用 image，如果为空则使用 images 中的第一张
            String dishImage = d.getImage();
            if ((dishImage == null || dishImage.trim().isEmpty()) && d.getImages() != null && !d.getImages().trim().isEmpty()) {
                String[] imgArray = d.getImages().split(",");
                if (imgArray.length > 0 && imgArray[0] != null && !imgArray[0].trim().isEmpty()) {
                    dishImage = imgArray[0].trim();
                }
            }
            oi.setDishImage(dishImage);
            oi.setPrice(d.getPrice());
            oi.setQuantity(c.getQuantity());
            oi.setAmount(amt);
            orderItemMapper.insert(oi);
        }

        // 清空购物车
        cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId).eq(Cart::getStoreId, storeId));
        
        // 发送下单成功通知给用户（失败不影响主流程）
        try {
            notificationService.sendNotification(userId, "下单成功", "您的订单已提交，等待商家接单", "order", order.getId());
        } catch (Exception e) {
            log.warn("发送下单成功通知失败: {}", e.getMessage());
        }
        
        // 发送新订单通知给商家（失败不影响主流程）
        try {
            Store store = storeMapper.selectById(storeId);
            if (store != null && store.getUserId() != null) {
                notificationService.sendNotification(store.getUserId(), "新订单", "您有新的订单待处理，请及时接单", "order", order.getId());
            }
        } catch (Exception e) {
            log.warn("发送新订单通知给商家失败: {}", e.getMessage());
        }
        
        return order;
    }

    @Override
    public Page<Orders> myOrders(Long userId, Integer status, int page, int size) {
        var q = new LambdaQueryWrapper<Orders>().eq(Orders::getUserId, userId).orderByDesc(Orders::getCreateTime);
        if (status != null) q.eq(Orders::getStatus, status);
        return ordersMapper.selectPage(new Page<>(page, size), q);
    }

    @Override
    public Orders detail(Long userId, Long orderId) {
        Orders o = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>().eq(Orders::getId, orderId).eq(Orders::getUserId, userId));
        if (o != null) {
            List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
            // 为每个商品添加评价状态
            for (OrderItem item : items) {
                var reviewQ = new LambdaQueryWrapper<ProductReview>()
                    .eq(ProductReview::getOrderItemId, item.getId())
                    .eq(ProductReview::getUserId, userId);
                long count = productReviewMapper.selectCount(reviewQ);
                item.setReviewed(count > 0 ? 1 : 0);
            }
            o.setItems(items);
        }
        return o;
    }

    @Override
    public String createPayForm(Long userId, Long orderId) {
        Orders o = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>().eq(Orders::getId, orderId).eq(Orders::getUserId, userId));
        if (o == null) throw new RuntimeException("订单不存在");
        if (o.getStatus() != Constants.ORDER_STATUS_UNPAID) throw new RuntimeException("订单状态不允许支付");
        Store s = storeMapper.selectById(o.getStoreId());
        
        if (alipayEnabled) {
            return alipayService.createPayForm(o.getOrderNo(), "外卖订单-" + (s != null ? s.getName() : ""), o.getPayAmount().toString());
        } else {
            paySuccess(userId, orderId);
            return "success";
        }
    }

    @Transactional
    @Override
    public void handleAlipayNotify(Map<String, String> params) {
        log.info("========== 支付宝异步通知开始 ==========");
        log.info("通知参数: {}", params);
        
        // 跳过支付宝验签，直接处理
        String orderNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        
        log.info("订单号: {}, 交易状态: {}", orderNo, tradeStatus);
        
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            log.warn("交易状态不是成功状态，忽略处理: {}", tradeStatus);
            return;
        }

        Orders o = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>().eq(Orders::getOrderNo, orderNo));
        if (o == null) {
            log.error("未找到订单，订单号: {}", orderNo);
            return;
        }
        
        log.info("当前订单状态: {}", o.getStatus());
        
        if (o.getStatus() == Constants.ORDER_STATUS_UNPAID) {
            o.setStatus(Constants.ORDER_STATUS_PAID);
            o.setPayType("alipay");
            o.setPayTime(LocalDateTime.now());
            o.setTradeNo(params.get("trade_no"));
            ordersMapper.updateById(o);
            log.info("订单状态更新为已支付，订单号: {}", orderNo);
        } else {
            log.warn("订单状态不是待支付，不更新。当前状态: {}, 订单号: {}", o.getStatus(), orderNo);
        }
        
        log.info("========== 支付宝异步通知结束 ==========");
    }

    @Transactional
    @Override
    public void paySuccess(Long userId, Long orderId) {
        Orders o = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>().eq(Orders::getId, orderId).eq(Orders::getUserId, userId));
        if (o != null && o.getStatus() == Constants.ORDER_STATUS_UNPAID) {
            o.setStatus(Constants.ORDER_STATUS_PAID);
            o.setPayType("alipay");
            o.setPayTime(LocalDateTime.now());
            ordersMapper.updateById(o);
            
            // 发送支付成功通知给用户（失败不影响主流程）
            try {
                notificationService.sendNotification(userId, "支付成功", "订单已支付，等待商家接单", "order", orderId);
            } catch (Exception e) {
                log.warn("发送支付成功通知失败: {}", e.getMessage());
            }
            
            // 发送新订单通知给商家（失败不影响主流程）
            try {
                Store store = storeMapper.selectById(o.getStoreId());
                if (store != null && store.getUserId() != null) {
                    notificationService.sendNotification(store.getUserId(), "新订单", "您有新的订单待处理，请及时接单", "order", orderId);
                }
            } catch (Exception e) {
                log.warn("发送新订单通知给商家失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 查询支付状态（主动查询支付宝）
     */
    @Override
    public boolean queryPayStatus(Long userId, String orderNo) {
        Orders o = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>()
            .eq(Orders::getOrderNo, orderNo)
            .eq(Orders::getUserId, userId));
        
        if (o == null) return false;
        if (o.getStatus() != Constants.ORDER_STATUS_UNPAID) return true;
        
        // 订单仍为待支付，主动查询支付宝
        if (alipayEnabled) {
            boolean paid = alipayService.queryPaid(orderNo);
            if (paid) {
                // 支付宝已支付但本地未更新，手动更新
                paySuccess(userId, o.getId());
                return true;
            }
        }
        return false;
    }

    @Override
    public void cancel(Long userId, Long orderId) {
        Orders o = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>().eq(Orders::getId, orderId).eq(Orders::getUserId, userId));
        if (o != null && o.getStatus() == Constants.ORDER_STATUS_UNPAID) {
            o.setStatus(Constants.ORDER_STATUS_CANCELLED);
            ordersMapper.updateById(o);
            
            // 发送订单取消通知给商家（失败不影响主流程）
            try {
                Store store = storeMapper.selectById(o.getStoreId());
                if (store != null && store.getUserId() != null) {
                    notificationService.sendNotification(store.getUserId(), "订单已取消", "用户已取消订单", "order", orderId);
                }
            } catch (Exception e) {
                log.warn("发送订单取消通知失败: {}", e.getMessage());
            }
        }
    }

    @Transactional
    @Override
    public void refund(Long userId, Long orderId) {
        Orders o = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>().eq(Orders::getId, orderId).eq(Orders::getUserId, userId));
        if (o == null) {
            throw new RuntimeException("订单不存在");
        }
        if (o.getStatus() != Constants.ORDER_STATUS_PAID && o.getStatus() != Constants.ORDER_STATUS_MAKING) {
            throw new RuntimeException("订单状态不允许退款");
        }
        
        // 更新订单状态为已退款
        o.setStatus(Constants.ORDER_STATUS_REFUNDED);
        ordersMapper.updateById(o);
        
        // 发送退款成功通知给用户（失败不影响主流程）
        try {
            notificationService.sendNotification(userId, "退款成功", 
                String.format("订单已退款，退款金额 ¥%.2f 将原路退回", o.getTotalAmount()), "refund", orderId);
        } catch (Exception e) {
            log.warn("发送退款通知给用户失败: {}", e.getMessage());
        }
        
        // 发送退款通知给商家（失败不影响主流程）
        try {
            Store store = storeMapper.selectById(o.getStoreId());
            if (store != null && store.getUserId() != null) {
                notificationService.sendNotification(store.getUserId(), "订单已退款", 
                    String.format("用户申请退款成功，订单金额 ¥%.2f", o.getTotalAmount()), "refund", orderId);
            }
        } catch (Exception e) {
            log.warn("发送退款通知给商家失败: {}", e.getMessage());
        }
    }
}