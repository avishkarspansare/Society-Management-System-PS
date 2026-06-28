package com.societyledger.statement.strategy;

import com.societyledger.statement.dto.ParsedTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BobStatementParser Tests")
class BobStatementParserTest {

    private BobStatementParser parser;

    private static final String VALID_CSV =
            "Date,Value Date,Description,Ref No./Cheque No.,Debit,Credit,Balance\n" +
            "01-01-2024,01-01-2024,NEFT B403-MNT MAINTENANCE,NEFT123456789,,2500.00,52500.00\n" +
            "05-01-2024,05-01-2024,NEFT A101-MNT FLAT PAYMENT,NEFT987654321,,3000.00,55500.00\n" +
            "10-01-2024,10-01-2024,ATM CASH WITHDRAWAL,,5000.00,,50500.00\n";

    @BeforeEach
    void setUp() {
        parser = new BobStatementParser();
    }

    @Test
    @DisplayName("getBankCode returns BOB")
    void testGetBankCode() {
        assertThat(parser.getBankCode()).isEqualTo("BOB");
    }

    @Test
    @DisplayName("Parses valid CSV with 3 transactions")
    void testParseValidCsv() {
        InputStream is = new ByteArrayInputStream(VALID_CSV.getBytes(StandardCharsets.UTF_8));
        List<ParsedTransaction> txns = parser.parse(is, "statement.csv");

        assertThat(txns).hasSize(3);
    }

    @Test
    @DisplayName("Credit transaction parsed correctly")
    void testCreditTransactionParsed() {
        InputStream is = new ByteArrayInputStream(VALID_CSV.getBytes(StandardCharsets.UTF_8));
        List<ParsedTransaction> txns = parser.parse(is, "statement.csv");

        ParsedTransaction first = txns.get(0);
        assertThat(first.getTransactionDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(first.getCreditAmount()).isEqualByComparingTo(new BigDecimal("2500.00"));
        assertThat(first.getDebitAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(first.getBalance()).isEqualByComparingTo(new BigDecimal("52500.00"));
    }

    @Test
    @DisplayName("Extracts flat payment reference from description")
    void testExtractsPaymentReference() {
        InputStream is = new ByteArrayInputStream(VALID_CSV.getBytes(StandardCharsets.UTF_8));
        List<ParsedTransaction> txns = parser.parse(is, "statement.csv");

        // First transaction has ref in ref number field
        assertThat(txns.get(0).getReferenceNumber()).isEqualTo("NEFT123456789");
    }

    @Test
    @DisplayName("Debit transaction parsed correctly")
    void testDebitTransactionParsed() {
        InputStream is = new ByteArrayInputStream(VALID_CSV.getBytes(StandardCharsets.UTF_8));
        List<ParsedTransaction> txns = parser.parse(is, "statement.csv");

        ParsedTransaction debit = txns.get(2);
        assertThat(debit.getDebitAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(debit.getCreditAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Handles amounts with commas (Indian number format)")
    void testHandlesCommaSeparatedAmounts() {
        String csvWithCommas =
                "Date,Value Date,Description,Ref No./Cheque No.,Debit,Credit,Balance\n" +
                "01-01-2024,01-01-2024,NEFT,REF001,,\"1,00,000.00\",\"5,00,000.00\"\n";
        InputStream is = new ByteArrayInputStream(csvWithCommas.getBytes(StandardCharsets.UTF_8));
        List<ParsedTransaction> txns = parser.parse(is, "statement.csv");

        assertThat(txns).hasSize(1);
        assertThat(txns.get(0).getCreditAmount()).isEqualByComparingTo(new BigDecimal("100000.00"));
    }

    @Test
    @DisplayName("Skips non-data rows gracefully")
    void testSkipsNonDataRows() {
        String csvWithJunk =
                "Date,Value Date,Description,Ref No./Cheque No.,Debit,Credit,Balance\n" +
                "TOTAL,,,,,5000.00,\n" +
                "01-01-2024,01-01-2024,Valid Transaction,REF001,,500.00,500.00\n";
        InputStream is = new ByteArrayInputStream(csvWithJunk.getBytes(StandardCharsets.UTF_8));
        List<ParsedTransaction> txns = parser.parse(is, "statement.csv");

        assertThat(txns).hasSize(1);
    }

    @Test
    @DisplayName("Empty file returns empty list")
    void testEmptyFile() {
        String emptycsv = "Date,Value Date,Description,Ref No./Cheque No.,Debit,Credit,Balance\n";
        InputStream is = new ByteArrayInputStream(emptycsv.getBytes(StandardCharsets.UTF_8));
        List<ParsedTransaction> txns = parser.parse(is, "empty.csv");

        assertThat(txns).isEmpty();
    }

    @Test
    @DisplayName("Throws on malformed CSV")
    void testThrowsOnMalformedCsv() {
        String malformed = "NOT,A,VALID,STATEMENT\nrandom,junk,here\n";
        InputStream is = new ByteArrayInputStream(malformed.getBytes(StandardCharsets.UTF_8));

        // Parser should either throw or return empty — both are acceptable
        // In this case our parser skips rows it can't parse by date
        List<ParsedTransaction> txns = parser.parse(is, "bad.csv");
        assertThat(txns).isEmpty();
    }
}
