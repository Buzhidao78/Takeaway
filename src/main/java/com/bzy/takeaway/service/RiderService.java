package com.bzy.takeaway.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bzy.takeaway.entity.DeliveryOrder;
import com.bzy.takeaway.entity.Rider;
import com.bzy.takeaway.entity.RiderEarnings;
import com.bzy.takeaway.entity.RiderLocation;
import com.bzy.takeaway.entity.RiderOnline;

import java.util.List;
import java.util.Map;

public interface RiderService {

    Rider getRiderInfo(Long userId);

    boolean isRider(Long userId);

    RiderOnline getRiderOnline(Long riderId);

    void toggleOnline(Long riderId, boolean online);

    void heartbeat(Long riderId);

    boolean isOnline(Long riderId);

    List<DeliveryOrder> getMyDeliveryOrders(Long riderId, Integer status);

    List<DeliveryOrder> getMyDeliveryOrders(Long riderId, Integer status, Integer... statuses);

    Map<String, Object> getTodayStats(Long riderId);

    Page<Rider> listRiders(int page, int size, String keyword, Integer status);

    Rider verify(Long riderId, Integer auditStatus);

    List<Rider> getAvailableRiders();

    DeliveryOrder assignRider(Long orderId, Long riderId);

    RiderLocation getLocation(Long riderId);

    Rider register(Rider rider);

    Rider login(String phone, String password);

    Rider getProfile(Long riderId);

    Rider updateProfile(Long riderId, Rider rider);

    Rider updateStatus(Long riderId, Integer status);

    void updateLocation(Long riderId, RiderLocation location);

    List<DeliveryOrder> getOrders(Long riderId, Integer status);

    List<DeliveryOrder> getAvailableOrders();

    DeliveryOrder acceptOrder(Long riderId, Long id);

    DeliveryOrder pickUp(Long riderId, Long id);

    DeliveryOrder complete(Long riderId, Long id);

    Page<RiderEarnings> getEarnings(Long riderId, int page, int size);
}