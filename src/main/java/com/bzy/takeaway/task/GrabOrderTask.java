package com.bzy.takeaway.task;

import com.bzy.takeaway.service.GrabOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrabOrderTask {

    private final GrabOrderService grabOrderService;

    @Scheduled(fixedRate = 60000)
    public void cleanExpiredOrders() {
        log.info("开始清理过期抢单订单");
        grabOrderService.removeExpiredOrders();
    }
}
