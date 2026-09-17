package com.bzy.takeaway.service;

import com.bzy.takeaway.entity.DeliveryZone;
import com.bzy.takeaway.entity.MerchantDeliveryConfig;

import java.util.List;

public interface DeliveryConfigService {

    /**
     * 获取商家配送配置
     */
    MerchantDeliveryConfig getConfig(Long merchantId);

    /**
     * 保存商家配送配置
     */
    void saveConfig(MerchantDeliveryConfig config);

    /**
     * 获取商家的配送区域列表
     */
    List<DeliveryZone> getZones(Long merchantId);

    /**
     * 保存配送区域
     */
    void saveZone(DeliveryZone zone);

    /**
     * 删除配送区域
     */
    void deleteZone(Long id);

    /**
     * 根据地址查询配送区域
     */
    DeliveryZone findZone(Long merchantId, String province, String city, String district, String zoneLabel);
}
