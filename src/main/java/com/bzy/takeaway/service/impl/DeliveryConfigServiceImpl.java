package com.bzy.takeaway.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bzy.takeaway.entity.DeliveryZone;
import com.bzy.takeaway.entity.MerchantDeliveryConfig;
import com.bzy.takeaway.mapper.DeliveryZoneMapper;
import com.bzy.takeaway.mapper.MerchantDeliveryConfigMapper;
import com.bzy.takeaway.service.DeliveryConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryConfigServiceImpl implements DeliveryConfigService {

    private final MerchantDeliveryConfigMapper merchantDeliveryConfigMapper;
    private final DeliveryZoneMapper deliveryZoneMapper;

    /**
     * 获取商家配送配置
     */
    @Override
    public MerchantDeliveryConfig getConfig(Long merchantId) {
        return merchantDeliveryConfigMapper.selectOne(
            new LambdaQueryWrapper<MerchantDeliveryConfig>()
                .eq(MerchantDeliveryConfig::getMerchantId, merchantId)
        );
    }

    /**
     * 保存商家配送配置
     */
    @Override
    @Transactional
    public void saveConfig(MerchantDeliveryConfig config) {
        MerchantDeliveryConfig existing = merchantDeliveryConfigMapper.selectOne(
            new LambdaQueryWrapper<MerchantDeliveryConfig>()
                .eq(MerchantDeliveryConfig::getMerchantId, config.getMerchantId())
        );

        if (existing != null) {
            config.setId(existing.getId());
            merchantDeliveryConfigMapper.updateById(config);
        } else {
            merchantDeliveryConfigMapper.insert(config);
        }
    }

    /**
     * 获取商家的配送区域列表
     */
    @Override
    public List<DeliveryZone> getZones(Long merchantId) {
        return deliveryZoneMapper.selectList(
            new LambdaQueryWrapper<DeliveryZone>()
                .eq(DeliveryZone::getMerchantId, merchantId)
                .orderByAsc(DeliveryZone::getSortOrder)
        );
    }

    /**
     * 保存配送区域
     */
    @Override
    public void saveZone(DeliveryZone zone) {
        if (zone.getId() != null) {
            deliveryZoneMapper.updateById(zone);
        } else {
            deliveryZoneMapper.insert(zone);
        }
    }

    /**
     * 删除配送区域
     */
    @Override
    public void deleteZone(Long id) {
        deliveryZoneMapper.deleteById(id);
    }

    /**
     * 根据地址查询配送区域
     */
    @Override
    public DeliveryZone findZone(Long merchantId, String province, String city, String district, String zoneLabel) {
        // 先尝试精确匹配（省+市+区+zoneLabel）
        if (zoneLabel != null && !zoneLabel.isEmpty()) {
            LambdaQueryWrapper<DeliveryZone> wrapper = new LambdaQueryWrapper<DeliveryZone>()
                .eq(DeliveryZone::getMerchantId, merchantId)
                .eq(DeliveryZone::getProvince, province)
                .eq(DeliveryZone::getCity, city)
                .eq(DeliveryZone::getDistrict, district)
                .eq(DeliveryZone::getZoneLabel, zoneLabel)
                .eq(DeliveryZone::getIsAvailable, 1);
            
            DeliveryZone zone = deliveryZoneMapper.selectOne(wrapper);
            if (zone != null) {
                return zone;
            }
        }
        
        // 如果没有找到精确匹配，或者 zoneLabel 为空，尝试匹配省+市+区（忽略zoneLabel）
        LambdaQueryWrapper<DeliveryZone> wrapper = new LambdaQueryWrapper<DeliveryZone>()
            .eq(DeliveryZone::getMerchantId, merchantId)
            .eq(DeliveryZone::getProvince, province)
            .eq(DeliveryZone::getCity, city)
            .eq(DeliveryZone::getDistrict, district)
            .eq(DeliveryZone::getIsAvailable, 1);
        
        return deliveryZoneMapper.selectOne(wrapper);
    }
}
