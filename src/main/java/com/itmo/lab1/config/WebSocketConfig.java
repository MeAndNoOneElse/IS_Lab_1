package com.itmo.lab1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Конфигурация WebSocket (STOMP) для синхронизации интерфейса между клиентами в реальном времени.
 * <p>
 * Когда какое-либо существо, город или кольцо создаётся/редактируется/удаляется,
 * сервер рассылает уведомление в топик {@code /topic/creatures}. Все подключённые
 * клиенты получают его и обновляют свои страницы без перезагрузки.
 * <p>
 * Используется нативный WebSocket без SockJS: клиент подключается через
 * {@code new WebSocket()} + STOMP поверх него.
 */
@Configuration
@EnableWebSocketMessageBroker // включает поддержку STOMP-брокера сообщений
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /** Разрешённые источники для WebSocket — ограничено источником приложения (безопаснее, чем "*"). */
    private static final String[] ALLOWED_ORIGINS = {"http://localhost:8080"};

    /**
     * Регистрирует STOMP-ендпоинт {@code /ws}, по которому клиенты устанавливают WebSocket-соединение.
     * SockJS-обёртка не используется — клиент соединяется напрямую через нативный WebSocket.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(ALLOWED_ORIGINS);
    }
    /**
     * Настраивает сообщенийого брокера:
     * <ul>
     *   <li>{@code enableSimpleBroker("/topic")} — встроенный простой брокер.
     *       Сервер рассылает сообщения в топики вида {@code /topic/creatures}, клиенты на них подписываются;</li>
     *   <li>{@code setApplicationDestinationPrefixes("/app")} — префикс для сообщений,
     *       которые клиенты отправляют серверу (контроллеры с {@code @MessageMapping}).</li>
     * </ul>
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
