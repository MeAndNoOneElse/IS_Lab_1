package com.itmo.lab1.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Рассылает уведомления об изменении данных всем подключённым клиентам
 * через WebSocket-брокер /topic/creatures. Обеспечивает синхронизацию
 * интерфейсов у всех авторизованных пользователей в реальном времени.
 * <p>
 * Важный момент: рассылка откладывается до коммита текущей транзакции (если она
 * активна). Раньше уведомление отправлялось сразу внутри {@code @Transactional}
 * метода сервиса, до фиксации данных в БД. Второй клиент получал его, перезагружал
 * страницу и читал ещё старые данные — отсюда впечатление, что «синхронизация не
 * работает». Теперь сообщение уходит только после коммита, и перезагружающийся
 * клиент уже видит актуальное состояние. Если транзакции нет (например, в тестах),
 * отправка происходит сразу.
 */
@Component
public class ChangeBroadcaster {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void broadcastCreatureChanged() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronizationAdapter() {
                        @Override
                        public void afterCommit() {
                            messagingTemplate.convertAndSend("/topic/creatures",
                                    new ChangeEvent("creature", System.currentTimeMillis()));
                        }
                    });
        } else {
            messagingTemplate.convertAndSend("/topic/creatures",
                    new ChangeEvent("creature", System.currentTimeMillis()));
        }
    }

    public record ChangeEvent(String entity, long timestamp) {}
}
