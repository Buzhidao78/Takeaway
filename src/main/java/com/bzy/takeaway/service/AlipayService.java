package com.bzy.takeaway.service;

import java.util.Map;

/**
 * 支付宝沙箱支付服务
 */
public interface AlipayService {

    void init();

    /**
     * 创建支付表单HTML（跳转支付宝收银台）
     */
    String createPayForm(String orderNo, String subject, String totalAmount);

    /**
     * 异步通知验签
     */
    boolean verifyNotify(Map<String, String> params);

    /**
     * 查询订单支付状态
     */
    boolean queryPaid(String orderNo);
}
