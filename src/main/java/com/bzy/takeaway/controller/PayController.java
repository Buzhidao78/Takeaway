package com.bzy.takeaway.controller;

import com.bzy.takeaway.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付回调 - 支付宝异步通知
 * natapp 穿透后需将 notify-url 配置为: http://你的域名/api/pay/alipay/notify
 */
@Slf4j
@RestController
@RequestMapping("/pay/alipay")
@RequiredArgsConstructor
public class PayController {

    private final OrderService orderService;

    @PostMapping("/notify")
    public String alipayNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v != null && v.length > 0 ? v[0] : ""));
        log.info("支付宝异步通知: {}", params);
        orderService.handleAlipayNotify(params);
        return "success";
    }
}
