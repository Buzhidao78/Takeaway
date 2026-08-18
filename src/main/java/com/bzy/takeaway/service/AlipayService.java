package com.bzy.takeaway.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Map;

/**
 * 支付宝沙箱支付服务
 */
@Slf4j
@Service
public class AlipayService {

    @Value("${alipay.enabled:false}")
    private boolean enabled;
    @Value("${alipay.app-id:}")
    private String appId;
    @Value("${alipay.private-key:}")
    private String privateKey;
    @Value("${alipay.alipay-public-key:}")
    private String alipayPublicKey;
    @Value("${alipay.gateway-url:https://openapi-sandbox.dl.alipaydev.com/gateway.do}")
    private String gatewayUrl;
    @Value("${alipay.notify-url:}")
    private String notifyUrl;
    @Value("${alipay.return-url:}")
    private String returnUrl;

    private AlipayClient alipayClient;

    @PostConstruct
    public void init() {
        if (!enabled) {
            log.warn("支付宝支付未启用（alipay.enabled=false），请检查配置文件");
            return;
        }
        if (appId != null && !appId.isEmpty() && !appId.contains("你的")) {
            // 自定义密钥模式：需要配置私钥和支付宝公钥
            if (privateKey == null || privateKey.isEmpty()) {
                log.error("自定义密钥模式下，私钥不可为空");
                throw new RuntimeException("支付宝私钥未配置");
            }
            if (alipayPublicKey == null || alipayPublicKey.isEmpty()) {
                log.error("自定义密钥模式下，支付宝公钥不可为空");
                throw new RuntimeException("支付宝公钥未配置");
            }
            // 支付宝 SDK 直接使用原始密钥字符串，不需要 PEM 格式化
            alipayClient = new DefaultAlipayClient(gatewayUrl, appId, privateKey, "json", "UTF-8", alipayPublicKey, "RSA2");
            log.info("支付宝沙箱支付已初始化（自定义密钥模式），app-id: {}", appId);
        } else {
            log.warn("支付宝沙箱未配置，请配置 alipay.app-id、private-key、alipay-public-key");
        }
    }

    /**
     * 创建支付表单HTML（跳转支付宝收银台）
     */
    public String createPayForm(String orderNo, String subject, String totalAmount) {
        if (alipayClient == null) {
            throw new RuntimeException("支付宝未配置，请在 application.yml 中配置沙箱参数");
        }
        try {
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(notifyUrl);
            request.setReturnUrl(returnUrl);
            request.setBizContent("{\"out_trade_no\":\"" + orderNo + "\",\"total_amount\":\"" + totalAmount + "\",\"subject\":\"" + subject + "\",\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
            return alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            log.error("支付宝下单失败", e);
            throw new RuntimeException("支付创建失败");
        }
    }

    /**
     * 异步通知验签
     */
    public boolean verifyNotify(Map<String, String> params) {
        if (alipayPublicKey == null || alipayPublicKey.isEmpty()) return false;
        try {
            return AlipaySignature.rsaCheckV1(params, alipayPublicKey, "UTF-8", "RSA2");
        } catch (AlipayApiException e) {
            return false;
        }
    }

    /**
     * 查询订单支付状态
     */
    public boolean queryPaid(String orderNo) {
        if (alipayClient == null) return false;
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            request.setBizContent("{\"out_trade_no\":\"" + orderNo + "\"}");
            AlipayTradeQueryResponse resp = alipayClient.execute(request);
            return resp.isSuccess() && "TRADE_SUCCESS".equals(resp.getTradeStatus());
        } catch (AlipayApiException e) {
            return false;
        }
    }
}
