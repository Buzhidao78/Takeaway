package com.bzy.takeaway.service;

import com.bzy.takeaway.entity.DeliveryOrder;
import com.bzy.takeaway.websocket.RiderWebSocketHandler;

import java.util.List;
import java.util.Set;

public interface GrabOrderService {

    void setRiderWebSocketHandler(RiderWebSocketHandler riderWebSocketHandler);

    void addToGrabPool(Long deliveryOrderId);

    boolean grabOrder(Long deliveryOrderId, Long riderId);

    List<DeliveryOrder> getGrabList();

    void removeExpiredOrders();

    void setRiderOnline(Long riderId, boolean online);

    boolean isRiderOnline(Long riderId);

    Set<String> getOnlineRiders();
}