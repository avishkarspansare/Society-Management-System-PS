package com.societyledger.common.kafka;

/**
 * Central registry of all Kafka topic names.
 * Must be kept in sync with docker-compose Kafka topic creation.
 */
public final class KafkaTopics {

    private KafkaTopics() {}

    // Statement / Matching lifecycle
    public static final String STATEMENT_UPLOADED   = "society-ledger.statement.uploaded";
    public static final String PAYMENT_MATCHED      = "society-ledger.payment.matched";
    public static final String PAYMENT_UNMATCHED    = "society-ledger.payment.unmatched";

    // Receipt
    public static final String RECEIPT_GENERATED    = "society-ledger.receipt.generated";

    // Expense
    public static final String EXPENSE_PUBLISHED    = "society-ledger.expense.published";

    // Audit
    public static final String AUDIT_EVENTS         = "society-ledger.audit.events";

    // Query
    public static final String QUERY_EVENTS         = "society-ledger.query.events";

    // Notifications (consumed by notification-service)
    public static final String NOTIFICATION_SEND    = "society-ledger.notification.send";
}
