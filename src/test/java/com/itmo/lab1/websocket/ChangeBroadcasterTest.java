package com.itmo.lab1.websocket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Тесты {@link ChangeBroadcaster}: проверка времени отправки WebSocket-уведомления
 * относительно коммита транзакции.
 * <p>
 * Уведомление должно отправляться только после коммита транзакции, чтобы второй
 * клиент, получив его и перезагрузив страницу, уже видел актуальные данные в БД.
 */
@ExtendWith(MockitoExtension.class)
class ChangeBroadcasterTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private ChangeBroadcaster broadcaster;

    @BeforeEach
    void setUp() {
        broadcaster = new ChangeBroadcaster();
        ReflectionTestUtils.setField(broadcaster, "messagingTemplate", messagingTemplate);
        resetSyncState();
    }

    @AfterEach
    void tearDown() {
        resetSyncState();
    }

    private void resetSyncState() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void broadcastCreatureChangedWithoutTransactionSendsImmediately() {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());

        broadcaster.broadcastCreatureChanged();

        verify(messagingTemplate, times(1))
                .convertAndSend(anyString(), any(ChangeBroadcaster.ChangeEvent.class));
    }

    @Test
    void broadcastCreatureChangedWithActiveTransactionSendsAfterCommit() {
        // Имитируем активную транзакцию (как внутри @Transactional-метода сервиса).
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        assertTrue(TransactionSynchronizationManager.isActualTransactionActive());

        broadcaster.broadcastCreatureChanged();

        // Коммит ещё не произошёл — рассылка отложена, ничего не отправлено.
        verify(messagingTemplate, never())
                .convertAndSend(anyString(), any(ChangeBroadcaster.ChangeEvent.class));

        // Транзакция коммитится — менеджер транзакций вызывает afterCommit у синхронизации.
        assertEquals(1, TransactionSynchronizationManager.getSynchronizations().size());
        TransactionSynchronizationManager.getSynchronizations().get(0).afterCommit();

        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(1))
                .convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());
        assertEquals("/topic/creatures", destinationCaptor.getValue());
        assertTrue(payloadCaptor.getValue() instanceof ChangeBroadcaster.ChangeEvent);
        ChangeBroadcaster.ChangeEvent event = (ChangeBroadcaster.ChangeEvent) payloadCaptor.getValue();
        assertEquals("creature", event.entity());
        assertTrue(event.timestamp() > 0);
    }

    @Test
    void broadcastCreatureChangedWithActiveTransactionNotSentOnRollback() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        broadcaster.broadcastCreatureChanged();

        verify(messagingTemplate, never())
                .convertAndSend(anyString(), any(ChangeBroadcaster.ChangeEvent.class));

        // Откат транзакции — afterCommit не вызывается, уведомление не отправляется.
        TransactionSynchronizationManager.getSynchronizations().get(0)
                .afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);

        verify(messagingTemplate, never())
                .convertAndSend(anyString(), any(ChangeBroadcaster.ChangeEvent.class));
    }
}
