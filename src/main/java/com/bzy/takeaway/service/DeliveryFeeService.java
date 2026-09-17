package com.bzy.takeaway.service;

import java.math.BigDecimal;

public interface DeliveryFeeService {

    /**
     * 计算配送费
     * 
     * @param merchantId 商家 ID
     * @param province 省
     * @param city 市
     * @param district 区/县
     * @param zoneLabel 区域标签
     * @param goodsAmount 商品金额
     * @return 配送费金额
     */
    BigDecimal calculateDeliveryFee(
        Long merchantId,
        String province,
        String city,
        String district,
        String zoneLabel,
        BigDecimal goodsAmount
    );

    /**
     * 检查地址是否可配送
     */
    boolean canDeliver(Long merchantId, String province, String city, String district, String zoneLabel);
}
