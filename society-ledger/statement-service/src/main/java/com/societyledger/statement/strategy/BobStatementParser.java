package com.societyledger.statement.strategy;

import com.societyledger.statement.entity.BankStatement;
import com.societyledger.statement.entity.BankTransaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Bank of Baroda CSV statement parser.
 * Expected columns: Date, Description, Debit, Credit, Balance, Ref/Cheque No.
 */
@Slf4j
@Component("BOB_PARSER")
public class BobStatementParser implements StatementParserStrategy {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public List<BankTransaction> parse(BankStatement statement, MultipartFile file) {
        List<BankTransaction> transactions = new ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVParser csv = CSVFormat.DEFAULT.builder()
                     .setHeader().setSkipHeaderRecord(true).setTrim(true)
                     .build().parse(reader)) {

            for (CSVRecord record : csv) {
                try {
                    String creditStr = getOrEmpty(record, "Credit");
                    if (creditStr.isBlank() || creditStr.equals("0") || creditStr.equals("0.00"))
                        continue; // skip debits

                    BigDecimal amount = new BigDecimal(creditStr.replaceAll("[^0-9.]", ""));
                    if (amount.compareTo(BigDecimal.ZERO) <= 0) continue;

                    LocalDate txnDate = LocalDate.parse(
                            getOrEmpty(record, "Date").replace("-", "/"), DATE_FMT);

                    String refCode = getOrEmpty(record, "Ref/Cheque No");
                    String desc    = getOrEmpty(record, "Description");

                    transactions.add(BankTransaction.builder()
                            .statementId(statement.getId())
                            .societyId(statement.getSocietyId())
                            .transactionDate(txnDate)
                            .description(desc)
                            .amount(amount)
                            .referenceCode(refCode.isBlank() ? null : refCode)
                            .matchStatus(BankTransaction.MatchStatus.UNMATCHED)
                            .build());
                } catch (Exception e) {
                    log.warn("Skipping unparseable row in BoB statement: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse BoB statement: {}", e.getMessage(), e);
        }
        log.info("BoB parser: {} credit transactions found in statement {}",
                transactions.size(), statement.getId());
        return transactions;
    }

    private String getOrEmpty(CSVRecord record, String header) {
        try { return record.get(header).trim(); }
        catch (Exception e) { return ""; }
    }
}
