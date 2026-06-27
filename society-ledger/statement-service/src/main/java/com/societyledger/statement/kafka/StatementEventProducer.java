package com.societyledger.statement.kafka;

import com.societyledger.common.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatementEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishStatementUploaded(Long societyId, Long statementId, int txnCount) {
        var event = Map.of(
            "eventType",    "STATEMENT_UPLOADED",
            "societyId",    societyId,
            "statementId",  statementId,
            "txnCount",     txnCount
        );
        kafkaTemplate.send(KafkaTopics.STATEMENT_UPLOADED, String.valueOf(societyId), event);
        log.info("Statement-uploaded event sent: statement={} society={} txns={}",
                statementId, societyId, txnCount);
    }

    public void publishPaymentMatched(Long societyId, Long flatId, Long transactionId,
                                       BigDecimal amount, String txnDate, String refCode) {
        var event = Map.of(
            "eventType",       "PAYMENT_MATCHED",
            "societyId",       societyId,
            "flatId",          flatId,
            "transactionId",   transactionId,
            "amount",          amount.toPlainString(),
            "transactionDate", txnDate,
            "referenceCode",   refCode != null ? refCode : ""
        );
        kafkaTemplate.send(KafkaTopics.PAYMENT_MATCHED, String.valueOf(societyId), event);
        log.info("Payment-matched event sent: txn={} flat={} society={}", transactionId, flatId, societyId);
    }

    public void publishPaymentUnmatched(Long societyId, Long transactionId,
                                         BigDecimal amount, String txnDate) {
        var event = Map.of(
            "eventType",       "PAYMENT_UNMATCHED",
            "societyId",       societyId,
            "transactionId",   transactionId,
            "amount",          amount.toPlainString(),
            "transactionDate", txnDate
        );
        kafkaTemplate.send(KafkaTopics.PAYMENT_UNMATCHED, String.valueOf(societyId), event);
        log.warn("Payment-unmatched event sent: txn={} society={}", transactionId, societyId);
    }
}
