package com.societyledger.statement.service;

import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.statement.dto.response.BankStatementResponse;
import com.societyledger.statement.dto.response.TransactionResponse;
import com.societyledger.statement.entity.BankStatement;
import com.societyledger.statement.entity.BankTransaction;
import com.societyledger.statement.kafka.StatementEventProducer;
import com.societyledger.statement.repository.BankStatementRepository;
import com.societyledger.statement.repository.BankTransactionRepository;
import com.societyledger.statement.service.PaymentMatchingService;
import com.societyledger.statement.strategy.StatementParserStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementService {

    private final BankStatementRepository statementRepository;
    private final BankTransactionRepository transactionRepository;
    private final PaymentMatchingService matchingService;
    private final StatementEventProducer eventProducer;
    private final Map<String, StatementParserStrategy> parsers;

    @Value("${app.file-storage.base-path}")
    private String basePath;

    @Transactional
    public BankStatementResponse uploadAndParse(Long societyId, MultipartFile file,
                                                 String bankCode, Long adminUserId) {
        if (file == null || file.isEmpty())
            throw SocietyLedgerException.badRequest("Statement file is empty.", "EMPTY_FILE");

        StatementParserStrategy parser = parsers.get(bankCode + "_PARSER");
        if (parser == null)
            throw SocietyLedgerException.badRequest("No parser for bank: " + bankCode, "UNSUPPORTED_BANK");

        String stored = UUID.randomUUID() + "_" + sanitize(file.getOriginalFilename());
        Path dir  = Paths.get(basePath, "statements", societyId.toString());
        Path dest = dir.resolve(stored);
        try {
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new SocietyLedgerException("Failed to store statement: " + e.getMessage(),
                    "FILE_STORE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        BankStatement statement = statementRepository.save(BankStatement.builder()
                .societyId(societyId).bankCode(bankCode)
                .fileName(file.getOriginalFilename()).filePath(dest.toString())
                .uploadedBy(adminUserId).status(BankStatement.StatementStatus.UPLOADED)
                .build());

        // Parse and persist transactions
        List<BankTransaction> transactions = parser.parse(statement, file);
        transactionRepository.saveAll(transactions);

        // Update statement header
        statement.setTotalTransactions(transactions.size());
        statement.setStatus(BankStatement.StatementStatus.PARSED);
        statementRepository.save(statement);

        // Trigger async matching
        matchingService.matchAll(societyId, statement.getId());

        eventProducer.publishStatementUploaded(societyId, statement.getId(), transactions.size());
        return mapToResponse(statementRepository.save(statement));
    }

    @Transactional(readOnly = true)
    public PageResponse<BankStatementResponse> getStatements(Long societyId, Pageable pageable) {
        return PageResponse.from(
                statementRepository.findBySocietyIdOrderByUploadedAtDesc(societyId, pageable)
                                   .map(this::mapToResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getTransactions(Long societyId, Long statementId,
                                                              String matchStatus, Pageable pageable) {
        if (matchStatus != null) {
            BankTransaction.MatchStatus ms = BankTransaction.MatchStatus.valueOf(matchStatus.toUpperCase());
            return PageResponse.from(
                    transactionRepository.findByStatementIdAndMatchStatus(statementId, ms, pageable)
                                         .map(this::mapTxnToResponse));
        }
        return PageResponse.from(
                transactionRepository.findByStatementIdOrderByTransactionDateDesc(statementId, pageable)
                                     .map(this::mapTxnToResponse));
    }

    @Transactional
    public TransactionResponse manualMatch(Long societyId, Long txnId, Long flatId, Long adminUserId) {
        return matchingService.manualMatch(societyId, txnId, flatId, adminUserId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalIncomeForMonth(Long societyId, int year, int month) {
        return transactionRepository.sumCreditsBySocietyIdAndYearAndMonth(societyId, year, month)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public int getPaidFlatsCountForMonth(Long societyId, int year, int month) {
        return transactionRepository.countMatchedFlatsBySocietyIdAndYearAndMonth(societyId, year, month);
    }

    private String sanitize(String name) {
        return name == null ? "statement" : name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private BankStatementResponse mapToResponse(BankStatement s) {
        return BankStatementResponse.builder()
                .id(s.getId()).societyId(s.getSocietyId()).bankCode(s.getBankCode())
                .fileName(s.getFileName()).uploadedBy(s.getUploadedBy())
                .totalTransactions(s.getTotalTransactions())
                .matchedCount(s.getMatchedCount()).unmatchedCount(s.getUnmatchedCount())
                .status(s.getStatus()).uploadedAt(s.getUploadedAt())
                .build();
    }

    private TransactionResponse mapTxnToResponse(BankTransaction t) {
        return TransactionResponse.builder()
                .id(t.getId()).statementId(t.getStatementId()).societyId(t.getSocietyId())
                .transactionDate(t.getTransactionDate()).description(t.getDescription())
                .amount(t.getAmount()).referenceCode(t.getReferenceCode())
                .matchStatus(t.getMatchStatus()).matchedFlatId(t.getMatchedFlatId())
                .build();
    }
}
