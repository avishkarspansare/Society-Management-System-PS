package com.societyledger.notification.kafka;

import com.societyledger.notification.service.NotificationDispatchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationConsumer Unit Tests")
class NotificationConsumerTest {

    @Mock NotificationDispatchService dispatchService;
    @InjectMocks NotificationConsumer notificationConsumer;

    @Test
    @DisplayName("onReceiptGenerated delegates to dispatch service")
    void onReceiptGenerated_delegatesToDispatch() {
        Map<String, Object> event = Map.of(
                "receiptId", 1,
                "flatId", 5,
                "societyId", 10,
                "residentEmail", "resident@test.com",
                "amount", 2500.0,
                "receiptNumber", "RCP-001"
        );

        notificationConsumer.onReceiptGenerated(event);

        verify(dispatchService).sendReceiptNotification(any());
    }

    @Test
    @DisplayName("onExpensePublished delegates to dispatch service")
    void onExpensePublished_delegatesToDispatch() {
        Map<String, Object> event = Map.of(
                "expenseId", 1,
                "societyId", 10,
                "category", "Maintenance",
                "amount", 5000.0
        );

        notificationConsumer.onExpensePublished(event);

        verify(dispatchService).sendExpenseNotification(any());
    }

    @Test
    @DisplayName("onQueryAnswered delegates to dispatch service")
    void onQueryAnswered_delegatesToDispatch() {
        Map<String, Object> event = Map.of(
                "queryId", 1,
                "askedByEmail", "resident@test.com",
                "subject", "Water issue"
        );

        notificationConsumer.onQueryAnswered(event);

        verify(dispatchService).sendQueryAnsweredNotification(any());
    }
}
