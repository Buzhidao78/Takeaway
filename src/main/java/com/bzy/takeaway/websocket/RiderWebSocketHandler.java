package com.bzy.takeaway.websocket;

import com.bzy.takeaway.entity.Rider;
import com.bzy.takeaway.mapper.RiderMapper;
import com.bzy.takeaway.service.GrabOrderService;
import com.bzy.takeaway.service.RiderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiderWebSocketHandler extends TextWebSocketHandler {

    private static final Map<Long, WebSocketSession> riderSessions = new ConcurrentHashMap<>();

    private final RiderMapper riderMapper;
    private final RiderService riderService;
    private final GrabOrderService grabOrderService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        Rider rider = riderService.getRiderInfo(userId);
        if (rider == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        riderSessions.put(rider.getId(), session);
        grabOrderService.setRiderOnline(rider.getId(), true);

        log.info("骑手 {} 已连接 WebSocket", rider.getId());
        sendMessage(session, "connected", Map.of("riderId", rider.getId()));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) return;

        Rider rider = riderService.getRiderInfo(userId);
        if (rider == null) return;

        try {
            JsonNode json = objectMapper.readTree(message.getPayload());
            String type = json.get("type").asText();

            switch (type) {
                case "heartbeat":
                    riderService.heartbeat(rider.getId());
                    sendMessage(session, "pong", Map.of());
                    break;
                case "online":
                    boolean online = json.get("online").asBoolean();
                    grabOrderService.setRiderOnline(rider.getId(), online);
                    riderService.toggleOnline(rider.getId(), online);
                    sendMessage(session, "online_status", Map.of("online", online));
                    break;
                default:
                    log.warn("未知的 WebSocket 消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("处理 WebSocket 消息失败", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) return;

        Rider rider = riderService.getRiderInfo(userId);
        if (rider == null) return;

        riderSessions.remove(rider.getId());
        grabOrderService.setRiderOnline(rider.getId(), false);

        log.info("骑手 {} 已断开 WebSocket 连接", rider.getId());
    }

    public void sendNewOrderNotification(Long riderId, Object orderData) {
        WebSocketSession session = riderSessions.get(riderId);
        if (session != null && session.isOpen()) {
            try {
                sendMessage(session, "order:new", orderData);
            } catch (Exception e) {
                log.error("发送新订单通知失败", e);
            }
        }
    }

    public void sendOrderStatusNotification(Long riderId, Object statusData) {
        WebSocketSession session = riderSessions.get(riderId);
        if (session != null && session.isOpen()) {
            try {
                sendMessage(session, "order:status", statusData);
            } catch (Exception e) {
                log.error("发送订单状态通知失败", e);
            }
        }
    }

    private void sendMessage(WebSocketSession session, String type, Object data) throws Exception {
        Map<String, Object> message = Map.of(
                "type", type,
                "data", data,
                "timestamp", System.currentTimeMillis()
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
    }
}
