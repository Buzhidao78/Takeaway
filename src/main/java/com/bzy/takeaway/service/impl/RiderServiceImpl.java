package com.bzy.takeaway.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bzy.takeaway.common.Constants;
import com.bzy.takeaway.entity.DeliveryOrder;
import com.bzy.takeaway.entity.OrderStatusLog;
import com.bzy.takeaway.entity.Orders;
import com.bzy.takeaway.entity.Rider;
import com.bzy.takeaway.entity.RiderEarnings;
import com.bzy.takeaway.entity.RiderLocation;
import com.bzy.takeaway.entity.RiderOnline;
import com.bzy.takeaway.entity.Store;
import com.bzy.takeaway.entity.StoreEarnings;
import com.bzy.takeaway.mapper.DeliveryOrderMapper;
import com.bzy.takeaway.mapper.OrderStatusLogMapper;
import com.bzy.takeaway.mapper.OrdersMapper;
import com.bzy.takeaway.mapper.RiderEarningsMapper;
import com.bzy.takeaway.mapper.RiderLocationMapper;
import com.bzy.takeaway.mapper.RiderMapper;
import com.bzy.takeaway.mapper.RiderOnlineMapper;
import com.bzy.takeaway.mapper.StoreEarningsMapper;
import com.bzy.takeaway.mapper.StoreMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.bzy.takeaway.entity.SysUser;
import com.bzy.takeaway.mapper.SysUserMapper;
import com.bzy.takeaway.service.GrabOrderService;
import com.bzy.takeaway.service.NotificationService;
import com.bzy.takeaway.service.RiderService;
import com.bzy.takeaway.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiderServiceImpl implements RiderService {

    private final RiderMapper riderMapper;
    private final RiderOnlineMapper riderOnlineMapper;
    private final DeliveryOrderMapper deliveryOrderMapper;
    private final RiderLocationMapper riderLocationMapper;
    private final RiderEarningsMapper riderEarningsMapper;
    private final OrdersMapper ordersMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final GrabOrderService grabOrderService;
    private final SysUserMapper sysUserMapper;
    private final JwtUtil jwtUtil;
    private final NotificationService notificationService;
    private final StoreMapper storeMapper;
    private final StoreEarningsMapper storeEarningsMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Rider getRiderInfo(Long userId) {
        return riderMapper.selectOne(new LambdaQueryWrapper<Rider>()
                .eq(Rider::getUserId, userId)
                .orderByDesc(Rider::getCreateTime)
                .last("LIMIT 1"));
    }

    @Override
    public boolean isRider(Long userId) {
        return getRiderInfo(userId) != null;
    }

    @Override
    public RiderOnline getRiderOnline(Long riderId) {
        return riderOnlineMapper.selectOne(new LambdaQueryWrapper<RiderOnline>()
                .eq(RiderOnline::getRiderId, riderId));
    }

    @Override
    public void toggleOnline(Long riderId, boolean online) {
        if (online) {
            Rider rider = riderMapper.selectById(riderId);
            if (rider != null && rider.getAuditStatus() != 1) {
                throw new RuntimeException("您的骑手申请尚未通过审核，无法上线接单");
            }
        }
        
        // 如果要切换为离线，检查是否有未完成的配送订单
        if (!online) {
            List<DeliveryOrder> activeOrders = deliveryOrderMapper.selectList(
                new LambdaQueryWrapper<DeliveryOrder>()
                    .eq(DeliveryOrder::getRiderId, riderId)
                    .eq(DeliveryOrder::getGrabStatus, 1)
                    .in(DeliveryOrder::getStatus, 1, 2) // 1:待取餐, 2:配送中
            );
            
            if (!activeOrders.isEmpty()) {
                throw new RuntimeException("您有 " + activeOrders.size() + " 个配送中的订单，完成后才能切换为离线状态");
            }
        }
        
        RiderOnline riderOnline = riderOnlineMapper.selectOne(new LambdaQueryWrapper<RiderOnline>()
                .eq(RiderOnline::getRiderId, riderId));

        if (riderOnline == null) {
            riderOnline = new RiderOnline();
            riderOnline.setRiderId(riderId);
            riderOnline.setIsOnline(online ? 1 : 0);
            riderOnline.setLastHeartbeat(LocalDateTime.now());
            riderOnlineMapper.insert(riderOnline);
        } else {
            riderOnline.setIsOnline(online ? 1 : 0);
            riderOnline.setLastHeartbeat(LocalDateTime.now());
            riderOnlineMapper.updateById(riderOnline);
        }

        grabOrderService.setRiderOnline(riderId, online);
        log.info("骑手 {} 状态切换为: {}", riderId, online ? "在线" : "离线");
    }

    @Override
    public void heartbeat(Long riderId) {
        RiderOnline riderOnline = riderOnlineMapper.selectOne(new LambdaQueryWrapper<RiderOnline>()
                .eq(RiderOnline::getRiderId, riderId));

        if (riderOnline != null) {
            riderOnline.setLastHeartbeat(LocalDateTime.now());
            riderOnlineMapper.updateById(riderOnline);
        }

        grabOrderService.setRiderOnline(riderId, true);
    }

    @Override
    public boolean isOnline(Long riderId) {
        return grabOrderService.isRiderOnline(riderId);
    }

    @Override
    public List<DeliveryOrder> getMyDeliveryOrders(Long riderId, Integer status) {
        LambdaQueryWrapper<DeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryOrder::getRiderId, riderId)
               .eq(DeliveryOrder::getGrabStatus, 1);
        if (status != null) {
            wrapper.eq(DeliveryOrder::getStatus, status);
        }
        wrapper.orderByDesc(DeliveryOrder::getCreateTime);
        return deliveryOrderMapper.selectList(wrapper);
    }

    @Override
    public List<DeliveryOrder> getMyDeliveryOrders(Long riderId, Integer status, Integer... statuses) {
        LambdaQueryWrapper<DeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryOrder::getRiderId, riderId)
               .eq(DeliveryOrder::getGrabStatus, 1);
        if (status != null) {
            wrapper.eq(DeliveryOrder::getStatus, status);
        } else if (statuses != null && statuses.length > 0) {
            wrapper.in(DeliveryOrder::getStatus, (Object[]) statuses);
        }
        wrapper.orderByDesc(DeliveryOrder::getCreateTime);
        return deliveryOrderMapper.selectList(wrapper);
    }

    @Override
    public Map<String, Object> getTodayStats(Long riderId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(today, LocalTime.MAX);

        long todayOrders = deliveryOrderMapper.selectCount(new LambdaQueryWrapper<DeliveryOrder>()
                .eq(DeliveryOrder::getRiderId, riderId)
                .ge(DeliveryOrder::getCreateTime, startOfDay)
                .le(DeliveryOrder::getCreateTime, endOfDay));

        long completedOrders = deliveryOrderMapper.selectCount(new LambdaQueryWrapper<DeliveryOrder>()
                .eq(DeliveryOrder::getRiderId, riderId)
                .eq(DeliveryOrder::getStatus, 3)
                .ge(DeliveryOrder::getCreateTime, startOfDay)
                .le(DeliveryOrder::getCreateTime, endOfDay));

        BigDecimal todayEarnings = deliveryOrderMapper.selectList(new LambdaQueryWrapper<DeliveryOrder>()
                .eq(DeliveryOrder::getRiderId, riderId)
                .eq(DeliveryOrder::getStatus, 3)
                .ge(DeliveryOrder::getCreateTime, startOfDay)
                .le(DeliveryOrder::getCreateTime, endOfDay))
                .stream()
                .map(DeliveryOrder::getFee)
                .filter(fee -> fee != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> stats = new HashMap<>();
        stats.put("todayOrders", todayOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("todayEarnings", todayEarnings);

        // 获取累计收益（所有已完成订单的配送费总和）
        BigDecimal totalEarnings = deliveryOrderMapper.selectList(new LambdaQueryWrapper<DeliveryOrder>()
                .eq(DeliveryOrder::getRiderId, riderId)
                .eq(DeliveryOrder::getStatus, 3))
                .stream()
                .map(DeliveryOrder::getFee)
                .filter(fee -> fee != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalEarnings", totalEarnings);

        // 获取总单量（所有已完成订单数量）
        long totalOrders = deliveryOrderMapper.selectCount(new LambdaQueryWrapper<DeliveryOrder>()
                .eq(DeliveryOrder::getRiderId, riderId)
                .eq(DeliveryOrder::getStatus, 3));
        stats.put("totalOrders", totalOrders);

        // 获取账户余额
        Rider rider = riderMapper.selectById(riderId);
        stats.put("balance", rider != null ? rider.getBalance() : BigDecimal.ZERO);

        return stats;
    }

    @Override
    public Page<Rider> listRiders(int page, int size, String keyword, Integer status) {
        LambdaQueryWrapper<Rider> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w
                    .like(Rider::getName, keyword)
                    .or()
                    .like(Rider::getPhone, keyword));
        }
        if (status != null) {
            wrapper.eq(Rider::getStatus, status);
        }
        wrapper.orderByDesc(Rider::getCreateTime);
        return riderMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Rider verify(Long riderId, Integer auditStatus) {
        Rider rider = riderMapper.selectById(riderId);
        if (rider == null) {
            throw new RuntimeException("骑手不存在");
        }
        rider.setAuditStatus(auditStatus);
        riderMapper.updateById(rider);
        log.info("骑手 {} 审核状态更新为：{}", riderId, auditStatus);
        return rider;
    }

    @Override
    public List<Rider> getAvailableRiders() {
        return riderMapper.selectList(new LambdaQueryWrapper<Rider>()
                .eq(Rider::getStatus, 1)
                .eq(Rider::getAuditStatus, 1));
    }

    @Override
    public DeliveryOrder assignRider(Long orderId, Long riderId) {
        DeliveryOrder deliveryOrder = deliveryOrderMapper.selectOne(new LambdaQueryWrapper<DeliveryOrder>()
                .eq(DeliveryOrder::getOrderId, orderId));
        if (deliveryOrder == null) {
            throw new RuntimeException("配送单不存在");
        }
        deliveryOrder.setRiderId(riderId);
        deliveryOrder.setStatus(1);
        deliveryOrderMapper.updateById(deliveryOrder);
        log.info("订单 {} 分配给骑手 {}", orderId, riderId);
        return deliveryOrder;
    }

    @Override
    public RiderLocation getLocation(Long riderId) {
        return riderLocationMapper.selectOne(new LambdaQueryWrapper<RiderLocation>()
                .eq(RiderLocation::getRiderId, riderId));
    }

    @Transactional
    @Override
    public Rider register(Rider rider) {
        SysUser user;
        
        // 先检查是否已存在骑手记录（包括已删除的记录，忽略 @TableLogic）
        Rider existingRider = riderMapper.selectByPhoneIgnoreDeleted(rider.getPhone());
        log.info(">>> 骑手注册：查询到骑手记录={}, rider.deleted={}", 
            existingRider != null ? existingRider.getId() : "null",
            existingRider != null ? existingRider.getDeleted() : "null");
        
        if (existingRider != null) {
            // 检查该骑手关联的用户是否已被注销（忽略 @TableLogic）
            SysUser riderUser = sysUserMapper.selectByIdIgnoreDeleted(existingRider.getUserId());
            log.info(">>> 骑手注册：查询到用户={}, deleted={}, phone={}", 
                riderUser != null ? riderUser.getId() : "null",
                riderUser != null ? riderUser.getDeleted() : "null",
                riderUser != null ? riderUser.getPhone() : "null");
            
            if (riderUser == null || (riderUser.getDeleted() != null && riderUser.getDeleted() == 1)) {
                // 用户已注销或不存在，更新现有骑手记录
                log.info(">>> 检测到已注销用户的骑手记录，更新骑手 ID={}", existingRider.getId());
                
                // 不删除旧用户，而是更新旧用户的信息
                if (riderUser != null) {
                    log.info(">>> 更新旧用户信息，用户 ID={}", riderUser.getId());
                    riderUser.setPhone(rider.getPhone());
                    riderUser.setPassword(passwordEncoder.encode(rider.getPassword()));
                    riderUser.setNickname(rider.getName());
                    riderUser.setRole(Constants.ROLE_RIDER);
                    riderUser.setStatus(1);
                    riderUser.setDeleted(0);
                    riderUser.setUpdateTime(LocalDateTime.now());
                    sysUserMapper.updateByIdIgnoreDeleted(riderUser);
                    user = riderUser;
                    log.info(">>> 更新用户成功，用户ID={}", user.getId());
                } else {
                    // 用户不存在，创建新用户
                    user = new SysUser();
                    user.setPhone(rider.getPhone());
                    user.setPassword(passwordEncoder.encode(rider.getPassword()));
                    user.setNickname(rider.getName());
                    user.setRole(Constants.ROLE_RIDER);
                    user.setStatus(1);
                    sysUserMapper.insert(user);
                    log.info(">>> 创建新用户成功，用户ID={}", user.getId());
                }
                
                // 释放旧骑手记录的手机号唯一索引（将手机号截断并添加ID后缀）
                String basePhone = existingRider.getPhone().substring(0, Math.min(11, existingRider.getPhone().length()));
                String oldPhone = basePhone.substring(0, 11 - String.valueOf(existingRider.getId()).length() - 1) + "_" + existingRider.getId();
                riderMapper.updatePhoneIgnoreDeleted(existingRider.getId(), oldPhone);
                log.info(">>> 已释放旧骑手记录的手机号，新手机号={}", oldPhone);
                
                // 创建新的骑手记录（参考商家系统：保留旧记录，创建新记录）
                Rider newRider = new Rider();
                newRider.setUserId(user.getId());
                newRider.setName(rider.getName());
                newRider.setPhone(rider.getPhone());
                newRider.setIdCard(rider.getIdCard());
                newRider.setAvatar(rider.getAvatar());
                newRider.setStatus(0);
                newRider.setAuditStatus(0);
                newRider.setBalance(BigDecimal.ZERO);
                newRider.setTotalOrders(0);
                newRider.setRating(BigDecimal.ZERO);
                newRider.setDeleted(0);
                newRider.setCreateTime(LocalDateTime.now());
                newRider.setUpdateTime(LocalDateTime.now());
                riderMapper.insert(newRider);
                log.info(">>> 创建新骑手记录成功，骑手ID={}", newRider.getId());
                
                String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getRole());
                newRider.setToken(token);
                
                // 发送通知给所有管理员
                try {
                    List<SysUser> admins = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getRole, Constants.ROLE_ADMIN)
                        .eq(SysUser::getDeleted, 0));
                    for (SysUser admin : admins) {
                        notificationService.sendNotification(admin.getId(), "骑手注册申请",
                            String.format("骑手「%s」（%s）提交注册申请，请及时审核", rider.getName(), rider.getPhone()),
                            "audit", newRider.getId());
                    }
                    log.info("已向 {} 位管理员发送骑手注册通知", admins.size());
                } catch (Exception e) {
                    log.warn("发送骑手注册通知失败: {}", e.getMessage());
                }
                
                return newRider;
            }
            throw new RuntimeException("该手机号已注册为骑手");
        }
        
        // 检查手机号是否已被未删除的用户使用
        SysUser existingUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, rider.getPhone())
                .eq(SysUser::getDeleted, 0));
        
        if (existingUser != null) {
            // 用户已存在，直接使用现有用户
            user = existingUser;
            log.info("用户已存在，直接注册为骑手，用户ID={}", user.getId());
        } else {
            // 用户不存在，检查是否存在已注销的用户
            SysUser deletedUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getPhone, rider.getPhone())
                    .eq(SysUser::getDeleted, 1));
            if (deletedUser != null) {
                sysUserMapper.deletePhysical(deletedUser.getId());
                log.info("已物理删除已注销用户 ID={}", deletedUser.getId());
            }
            
            // 创建新用户
            user = new SysUser();
            user.setPhone(rider.getPhone());
            user.setPassword(passwordEncoder.encode(rider.getPassword()));
            user.setNickname(rider.getName());
            user.setRole(Constants.ROLE_RIDER);
            user.setStatus(1);
            sysUserMapper.insert(user);
            log.info("创建新用户，用户ID={}", user.getId());
        }
        
        rider.setUserId(user.getId());
        rider.setStatus(0);
        rider.setAuditStatus(0);
        rider.setBalance(BigDecimal.ZERO);
        rider.setTotalOrders(0);
        rider.setRating(BigDecimal.ZERO);
        riderMapper.insert(rider);
        
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getRole());
        rider.setToken(token);
        
        // 发送通知给所有管理员
        try {
            List<SysUser> admins = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, Constants.ROLE_ADMIN)
                .eq(SysUser::getDeleted, 0));
            for (SysUser admin : admins) {
                notificationService.sendNotification(admin.getId(), "骑手注册申请",
                    String.format("骑手「%s」（%s）提交注册申请，请及时审核", rider.getName(), rider.getPhone()),
                    "audit", rider.getId());
            }
            log.info("已向 {} 位管理员发送骑手注册通知", admins.size());
        } catch (Exception e) {
            log.warn("发送骑手注册通知失败: {}", e.getMessage());
        }
        
        return rider;
    }

    @Override
    public Rider login(String phone, String password) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, phone)
                .eq(SysUser::getRole, Constants.ROLE_RIDER)
                .eq(SysUser::getDeleted, 0));
        
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("手机号或密码错误");
        }
        
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }
        
        Rider rider = riderMapper.selectOne(new LambdaQueryWrapper<Rider>()
                .eq(Rider::getUserId, user.getId())
                .eq(Rider::getDeleted, 0)
                .orderByDesc(Rider::getCreateTime)
                .last("LIMIT 1"));
        
        if (rider == null) {
            throw new RuntimeException("骑手信息不存在");
        }
        
        // 生成 JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getRole());
        rider.setToken(token);
        
        return rider;
    }

    @Override
    public Rider getProfile(Long riderId) {
        return riderMapper.selectById(riderId);
    }

    @Override
    public Rider updateProfile(Long riderId, Rider rider) {
        Rider existing = riderMapper.selectById(riderId);
        if (existing == null) {
            throw new RuntimeException("骑手不存在");
        }
        existing.setName(rider.getName());
        existing.setAvatar(rider.getAvatar());
        existing.setIdCard(rider.getIdCard());
        riderMapper.updateById(existing);
        return existing;
    }

    @Override
    public Rider updateStatus(Long riderId, Integer status) {
        Rider rider = riderMapper.selectById(riderId);
        if (rider == null) {
            throw new RuntimeException("骑手不存在");
        }
        rider.setStatus(status);
        riderMapper.updateById(rider);
        return rider;
    }

    @Override
    public void updateLocation(Long riderId, RiderLocation location) {
        RiderLocation existing = riderLocationMapper.selectOne(new LambdaQueryWrapper<RiderLocation>()
                .eq(RiderLocation::getRiderId, riderId));
        
        if (existing == null) {
            location.setRiderId(riderId);
            riderLocationMapper.insert(location);
        } else {
            existing.setLatitude(location.getLatitude());
            existing.setLongitude(location.getLongitude());
            existing.setAddress(location.getAddress());
            riderLocationMapper.updateById(existing);
        }
    }

    @Override
    public List<DeliveryOrder> getOrders(Long riderId, Integer status) {
        LambdaQueryWrapper<DeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryOrder::getRiderId, riderId);
        if (status != null) {
            wrapper.eq(DeliveryOrder::getStatus, status);
        }
        return deliveryOrderMapper.selectList(wrapper);
    }

    @Override
    public List<DeliveryOrder> getAvailableOrders() {
        return deliveryOrderMapper.selectList(new LambdaQueryWrapper<DeliveryOrder>()
                .eq(DeliveryOrder::getGrabStatus, 0));
    }

    @Override
    public DeliveryOrder acceptOrder(Long riderId, Long id) {
        DeliveryOrder order = deliveryOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        order.setRiderId(riderId);
        order.setStatus(1);
        deliveryOrderMapper.updateById(order);
        
        // 发送骑手接单通知给商家（失败不影响主流程）
        try {
            Orders mainOrder = ordersMapper.selectById(order.getOrderId());
            if (mainOrder != null) {
                Store store = storeMapper.selectById(mainOrder.getStoreId());
                if (store != null && store.getUserId() != null) {
                    Rider rider = riderMapper.selectById(riderId);
                    String riderName = rider != null ? rider.getName() : "骑手";
                    notificationService.sendNotification(store.getUserId(), "骑手已接单", 
                        String.format("骑手%s已接单，将为您配送订单", riderName), "rider", mainOrder.getId());
                }
            }
        } catch (Exception e) {
            log.warn("发送骑手接单通知失败: {}", e.getMessage());
        }
        
        return order;
    }

    @Transactional
    @Override
    public DeliveryOrder pickUp(Long riderId, Long id) {
        DeliveryOrder order = deliveryOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        int oldStatus = order.getStatus();
        order.setStatus(2);
        order.setPickUpTime(LocalDateTime.now());
        deliveryOrderMapper.updateById(order);

        // 更新主订单状态为配送中
        Orders mainOrder = ordersMapper.selectById(order.getOrderId());
        if (mainOrder != null) {
            mainOrder.setStatus(3);
            ordersMapper.updateById(mainOrder);
            
            // 发送骑手取餐通知给用户（失败不影响主流程）
            try {
                notificationService.sendNotification(mainOrder.getUserId(), "骑手已取餐", "骑手已取餐，正在配送中", "order", mainOrder.getId());
            } catch (Exception e) {
                log.warn("发送骑手取餐通知失败: {}", e.getMessage());
            }
        }

        // 记录状态日志
        try {
            OrderStatusLog log = new OrderStatusLog();
            log.setOrderId(order.getId());
            log.setOrderType(2);
            log.setOldStatus(oldStatus);
            log.setNewStatus(2);
            log.setOperatorId(riderId);
            log.setOperatorType(3);
            log.setRemark("骑手已取餐，开始配送");
            orderStatusLogMapper.insert(log);
        } catch (Exception e) {
            log.warn("记录状态日志失败: {}", e.getMessage());
        }

        return order;
    }

    @Transactional
    @Override
    public DeliveryOrder complete(Long riderId, Long id) {
        DeliveryOrder order = deliveryOrderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        int oldStatus = order.getStatus();
        order.setStatus(3);
        order.setDeliveryTime(LocalDateTime.now());
        deliveryOrderMapper.updateById(order);

        // 更新主订单状态为已完成
        Orders mainOrder = ordersMapper.selectById(order.getOrderId());
        if (mainOrder != null) {
            mainOrder.setStatus(4);
            ordersMapper.updateById(mainOrder);
            
            // 发送订单完成通知给用户（失败不影响主流程）
            try {
                notificationService.sendNotification(mainOrder.getUserId(), "订单已完成", "订单已送达，请确认收货", "order", mainOrder.getId());
            } catch (Exception e) {
                log.warn("发送订单完成通知失败: {}", e.getMessage());
            }
            
            // 发送骑手送达通知给商家（失败不影响主流程）
            try {
                Store store = storeMapper.selectById(mainOrder.getStoreId());
                if (store != null && store.getUserId() != null) {
                    Rider rider = riderMapper.selectById(riderId);
                    String riderName = rider != null ? rider.getName() : "骑手";
                    notificationService.sendNotification(store.getUserId(), "订单已送达", 
                        String.format("骑手%s已将订单送达，订单已完成", riderName), "rider", mainOrder.getId());
                }
            } catch (Exception e) {
                log.warn("发送骑手送达通知失败: {}", e.getMessage());
            }
        }

        // 记录状态日志
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(order.getId());
        log.setOrderType(2);
        log.setOldStatus(oldStatus);
        log.setNewStatus(3);
        log.setOperatorId(riderId);
        log.setOperatorType(3);
        log.setRemark("骑手已送达，订单完成");
        orderStatusLogMapper.insert(log);

        // 增加骑手收益记录
        if (order.getFee() != null && order.getFee().compareTo(BigDecimal.ZERO) > 0) {
            RiderEarnings earnings = new RiderEarnings();
            earnings.setRiderId(riderId);
            earnings.setOrderId(order.getOrderId());
            earnings.setAmount(order.getFee());
            earnings.setType(1);
            riderEarningsMapper.insert(earnings);

            // 更新骑手余额
            Rider rider = riderMapper.selectById(riderId);
            if (rider != null) {
                rider.setBalance(rider.getBalance().add(order.getFee()));
                rider.setTotalOrders(rider.getTotalOrders() + 1);
                riderMapper.updateById(rider);
            }
        }

        // 增加商家收益记录
        if (mainOrder != null && mainOrder.getPayAmount() != null && mainOrder.getPayAmount().compareTo(BigDecimal.ZERO) > 0) {
            StoreEarnings storeEarnings = new StoreEarnings();
            storeEarnings.setStoreId(mainOrder.getStoreId());
            storeEarnings.setOrderId(mainOrder.getId());
            storeEarnings.setAmount(mainOrder.getPayAmount());
            storeEarnings.setType(1);
            storeEarningsMapper.insert(storeEarnings);

            // 更新商家余额和总订单数
            Store store = storeMapper.selectById(mainOrder.getStoreId());
            if (store != null) {
                if (store.getBalance() == null) store.setBalance(BigDecimal.ZERO);
                if (store.getTotalOrders() == null) store.setTotalOrders(0);
                store.setBalance(store.getBalance().add(mainOrder.getPayAmount()));
                store.setTotalOrders(store.getTotalOrders() + 1);
                storeMapper.updateById(store);
            }
        }

        return order;
    }

    @Override
    public Page<RiderEarnings> getEarnings(Long riderId, int page, int size) {
        Page<RiderEarnings> pageResult = riderEarningsMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<RiderEarnings>()
                        .eq(RiderEarnings::getRiderId, riderId)
                        .orderByDesc(RiderEarnings::getCreateTime));
        // 为每条收益记录设置订单号
        for (RiderEarnings earning : pageResult.getRecords()) {
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