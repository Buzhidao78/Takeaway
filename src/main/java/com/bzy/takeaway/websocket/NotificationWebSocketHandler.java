package com.bzy.takeaway.websocket;

import com.bzy.takeaway.entity.SysUser;
import com.bzy.takeaway.mapper.SysUserMapper;
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
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    private final SysUserMapper sysUserMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        userSessions.put(userId, session);
        log.info("用户 {} 已连接通知 WebSocket", userId);
        sendMessage(session, "connected", Map.of("userId", userId));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) return;

        try {
            Map<String, Object> json = objectMapper.readValue(message.getPayload(), Map.class);
            String type = (String) json.get("type");

            if ("heartbeat".equals(type)) {
                sendMessage(session, "pong", Map.of());
            }
        } catch (Exception e) {
            log.error("处理 WebSocket 消息失败", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) return;

        userSessions.remove(userId);
        log.info("用户 {} 已断开通知 WebSocket 连接", userId);
    }

    public void sendNotification(Long userId, Object data) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                sendMessage(session, "notification:new", data);
                log.info("向用户 {} 推送通知: {}", userId, data);
            } catch (Exception e) {
                log.error("发送通知失败", e);
            }
        } else {
            log.debug("用户 {} 未连接 WebSocket，跳过推送", userId);
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
