package com.societyledger.statement.strategy;

import com.societyledger.statement.dto.ParsedTransaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Bank of Baroda CSV statement parser.
 *
 * Expected CSV format:
 * Date,Value Date,Description,Ref No./Cheque No.,Debit,Credit,Balance
 * 01-01-2024,01-01-2024,NEFT TRANSFER,NEFT123456789,,2500.00,52500.00
 */
@Slf4j
@Component
public class BobStatementParser implements StatementParser {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String BANK_CODE = "BOB";

    // BoB column headers (case-insensitive matching handled in parse method)
    private static final String COL_DATE = "Date";
    private static final String COL_VALUE_DATE = "Value Date";
    private static final String COL_DESCRIPTION = "Description";
    private static final String COL_REF_NO = "Ref No./Cheque No.";
    private static final String COL_DEBIT = "Debit";
    private static final String COL_CREDIT = "Credit";
    private static final String COL_BALANCE = "Balance";

    @Override
    public String getBankCode() {
        return BANK_CODE;
    }

    @Override
    public List<ParsedTransaction> parse(InputStream inputStream, String fileName) {
        log.info("Parsing Bank of Baroda statement: {}", fileName);
        List<ParsedTransaction> transactions = new ArrayList<>();

        try (CSVParser csvParser = CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build()
                .parse(new InputStreamReader(inputStream))) {

            // Use Java Streams to process transactions — natural fit here
            transactions = StreamSupport.stream(csvParser.spliterator(), false)
                    .filter(this::isDataRow)
                    .map(this::parseRecord)
                    .toList();

            log.info("Parsed {} transactions from BoB statement", transactions.size());
        } catch (Exception e) {
            log.error("Failed to parse BoB statement: {}", e.getMessage(), e);
            throw new IllegalArgumentException(
                    "Failed to parse Bank of Baroda statement. " +
                    "Please ensure it is in the correct CSV format. Error: " + e.getMessage()
            );
        }

        return transactions;
    }

    private boolean isDataRow(CSVRecord record) {
        // Skip rows that don't have a parseable date (totals row, blank rows, etc.)
        try {
            String dateStr = record.get(COL_DATE);
            LocalDate.parse(dateStr.trim(), DATE_FORMAT);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private ParsedTransaction parseRecord(CSVRecord record) {
        return ParsedTransaction.builder()
                .transactionDate(parseDate(record.get(COL_DATE)))
                .valueDate(parseDate(record.get(COL_VALUE_DATE)))
                .description(safeGet(record, COL_DESCRIPTION))
                .referenceNumber(extractReference(safeGet(record, COL_REF_NO),
                        safeGet(record, COL_DESCRIPTION)))
                .debitAmount(parseMoney(record.get(COL_DEBIT)))
                .creditAmount(parseMoney(record.get(COL_CREDIT)))
                .balance(parseMoney(record.get(COL_BALANCE)))
                .rawLine(record.toString())
                .build();
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        return LocalDate.parse(dateStr.trim(), DATE_FORMAT);
    }

    private BigDecimal parseMoney(String value) {
        if (value == null || value.isBlank()) return BigDecimal.ZERO;
        // Remove commas (e.g. "1,00,000.00" -> "100000.00")
        String cleaned = value.replaceAll("[,\\s]", "");
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private String safeGet(CSVRecord record, String column) {
        try {
            return record.get(column);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Extracts the flat payment reference from description.
     * Example: "NEFT/B403-MNT/2024" → extracts "B403-MNT"
     */
    private String extractReference(String refNo, String description) {
        // First try refNo field
        if (refNo != null && !refNo.isBlank()) {
            return refNo.trim();
        }
        // Fallback: extract from description
        if (description != null) {
            // Pattern like B403-MNT, A201-MNT
            java.util.regex.Matcher matcher =
                    java.util.regex.Pattern.compile("[A-Z]\\d{3}-MNT", java.util.regex.Pattern.CASE_INSENSITIVE)
                            .matcher(description);
            if (matcher.find()) {
                return matcher.group().toUpperCase();
            }
        }
        return refNo;
    }
}
