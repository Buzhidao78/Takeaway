package com.bzy.takeaway.controller;

import com.bzy.takeaway.common.Result;
import com.bzy.takeaway.entity.DeliveryZone;
import com.bzy.takeaway.entity.MerchantDeliveryConfig;
import com.bzy.takeaway.entity.Store;
import com.bzy.takeaway.mapper.StoreMapper;
import com.bzy.takeaway.service.DeliveryConfigService;
import com.bzy.takeaway.service.DeliveryFeeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/merchant/delivery")
@RequiredArgsConstructor
@Slf4j
public class DeliveryConfigController {

    private final DeliveryConfigService deliveryConfigService;
    private final DeliveryFeeService deliveryFeeService;
    private final StoreMapper storeMapper;

    /**
     * 获取商家配送配置
     */
    @GetMapping("/config")
    public Result<MerchantDeliveryConfig> getConfig(@RequestAttribute("userId") Long userId) {
        MerchantDeliveryConfig config = deliveryConfigService.getConfig(userId);
        return Result.ok(config);
    }

    /**
     * 保存商家配送配置
     */
    @PostMapping("/config")
    public Result<Void> saveConfig(
        @RequestAttribute("userId") Long userId,
        @RequestBody MerchantDeliveryConfig config
    ) {
        config.setMerchantId(userId);
        deliveryConfigService.saveConfig(config);
        return Result.ok(null);
    }

    /**
     * 获取配送区域列表
     */
    @GetMapping("/zones")
    public Result<List<DeliveryZone>> getZones(@RequestAttribute("userId") Long userId) {
        List<DeliveryZone> zones = deliveryConfigService.getZones(userId);
        return Result.ok(zones);
    }

    /**
     * 保存配送区域
     */
    @PostMapping("/zones")
    public Result<Void> saveZone(
        @RequestAttribute("userId") Long userId,
        @RequestBody DeliveryZone zone
    ) {
        log.info("保存配送区域，userId: {}, 请求数据：{}", userId, zone);
        zone.setMerchantId(userId);
        deliveryConfigService.saveZone(zone);
        log.info("保存配送区域成功");
        return Result.ok(null);
    }

    /**
     * 删除配送区域
     */
    @DeleteMapping("/zones/{id}")
    public Result<Void> deleteZone(@PathVariable Long id) {
        deliveryConfigService.deleteZone(id);
        return Result.ok(null);
    }

    /**
     * 计算配送费
     */
    @PostMapping("/fee/calculate")
    public Result<Map<String, Object>> calculateFee(@RequestBody DeliveryFeeRequest request) {
        log.info("计算配送费请求：storeId={}, province={}, city={}, district={}, zoneLabel={}, goodsAmount={}",
            request.getStoreId(), request.getProvince(), request.getCity(), 
            request.getDistrict(), request.getZoneLabel(), request.getGoodsAmount());
        
        try {
            // 通过 storeId 查询商家 userId
            Store store = storeMapper.selectById(request.getStoreId());
            if (store == null) {
                throw new RuntimeException("商家不存在");
            }
            
            BigDecimal deliveryFee = deliveryFeeService.calculateDeliveryFee(
                store.getUserId(),
                request.getProvince(),
                request.getCity(),
                request.getDistrict(),
                request.getZoneLabel(),
                request.getGoodsAmount()
            );

            Map<String, Object> result = new HashMap<>();
            result.put("deliveryFee", deliveryFee);
            result.put("canDeliver", true);
            result.put("message", "配送费计算成功");
            log.info("配送费计算成功：{}", deliveryFee);

            return Result.ok(result);
        } catch (Exception e) {
            log.error("配送费计算失败：{}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("deliveryFee", BigDecimal.ZERO);
            result.put("canDeliver", false);
            result.put("message", e.getMessage());

            return Result.ok(result);
        }
    }

    @Data
    public static class DeliveryFeeRequest {
        private Long storeId;
        private String province;
        private String city;
        private String district;
        private String zoneLabel;
        private BigDecimal goodsAmount;
    }
}
