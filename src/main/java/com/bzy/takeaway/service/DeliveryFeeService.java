package com.bzy.takeaway.service;

import com.bzy.takeaway.entity.DeliveryZone;
import com.bzy.takeaway.entity.MerchantDeliveryConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryFeeService {

    private final DeliveryConfigService deliveryConfigService;

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
    public BigDecimal calculateDeliveryFee(
        Long merchantId,
        String province,
        String city,
        String district,
        String zoneLabel,
        BigDecimal goodsAmount
    ) {
        // 1. 获取商家配送配置
        MerchantDeliveryConfig config = deliveryConfigService.getConfig(merchantId);
        if (config == null || config.getIsEnabled() != 1) {
            throw new RuntimeException("商家未开通配送服务");
        }

        // 2. 检查最低起送金额
        if (goodsAmount.compareTo(config.getMinOrderAmount()) < 0) {
            throw new RuntimeException("未达到最低起送金额 ¥" + config.getMinOrderAmount());
        }

        // 3. 查找匹配的配送区域
        DeliveryZone zone = deliveryConfigService.findZone(merchantId, province, city, district, zoneLabel);
        if (zone == null) {
            throw new RuntimeException("该地址不在配送范围内");
        }

        // 4. 检查区域最低订单金额
        if (goodsAmount.compareTo(zone.getMinOrderAmount()) < 0) {
            throw new RuntimeException("该区域最低订单金额为 ¥" + zone.getMinOrderAmount());
        }

        // 5. 计算配送费
        BigDecimal baseFee = config.getBaseFee();
        BigDecimal additionalFee = zone.getAdditionalFee();
        BigDecimal freeThreshold = config.getFreeDeliveryThreshold();

        // 满免逻辑：订单金额 >= 门槛，免除基础配送费
        if (goodsAmount.compareTo(freeThreshold) >= 0) {
            baseFee = BigDecimal.ZERO;
            log.info("订单金额满 ¥{}，免除基础配送费", freeThreshold);
        }

        BigDecimal totalFee = baseFee.add(additionalFee);
        log.info("配送费计算：基础配送费 {} + 附加费 {} = {}", baseFee, additionalFee, totalFee);

        return totalFee;
    }

    /**
     * 检查地址是否可配送
     */
    public boolean canDeliver(Long merchantId, String province, String city, String district, String zoneLabel) {
        try {
            calculateDeliveryFee(merchantId, province, city, district, zoneLabel, BigDecimal.ZERO);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
