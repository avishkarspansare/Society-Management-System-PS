package com.societyledger.statement.service;

import com.societyledger.common.dto.PageResponse;
import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.statement.dto.ParsedTransaction;
import com.societyledger.statement.dto.request.UploadStatementRequest;
import com.societyledger.statement.dto.response.BankStatementResponse;
import com.societyledger.statement.entity.BankStatement;
import com.societyledger.statement.entity.BankTransaction;
import com.societyledger.statement.repository.BankStatementRepository;
import com.societyledger.statement.repository.BankTransactionRepository;
import com.societyledger.statement.strategy.StatementParser;
import com.societyledger.statement.strategy.StatementParserFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementService {

    private final BankStatementRepository statementRepository;
    private final BankTransactionRepository transactionRepository;
    private final StatementParserFactory parserFactory;
    private final PaymentMatchingService matchingService;

    private static final String UPLOAD_BASE_DIR = System.getProperty("user.home") + "/society-ledger/statements/";

    /**
     * Upload a bank statement, parse it, persist transactions, run matching engine.
     */
    @Transactional
    public BankStatementResponse uploadStatement(Long societyId, MultipartFile file,
                                                  String bankCode, Integer month, Integer year,
                                                  Long adminUserId) {
        if (file.isEmpty()) {
            throw SocietyLedgerException.badRequest("Statement file cannot be empty.");
        }

        // Persist file to local storage
        String filePath = saveFile(societyId, file);

        // Create statement record in PROCESSING state
        BankStatement statement = BankStatement.builder()
                .societyId(societyId)
                .bankName(bankCode.toUpperCase())
                .statementMonth(month)
                .statementYear(year)
                .filePath(filePath)
                .fileName(file.getOriginalFilename())
                .uploadStatus(BankStatement.UploadStatus.PROCESSING)
                .uploadedBy(adminUserId)
                .build();

        BankStatement saved = statementRepository.save(statement);

        // Parse using Strategy Pattern
        try {
            StatementParser parser = parserFactory.getParser(bankCode);
            List<ParsedTransaction> parsedTxns = parser.parse(file.getInputStream(),
                    file.getOriginalFilename());

            // Persist parsed transactions via stream pipeline
            List<BankTransaction> transactions = parsedTxns.stream()
                    .map(pt -> BankTransaction.builder()
                            .statement(saved)
                            .societyId(societyId)
                            .transactionDate(pt.getTransactionDate())
                            .valueDate(pt.getValueDate())
                            .description(pt.getDescription())
                            .referenceNumber(pt.getReferenceNumber())
                            .creditAmount(pt.getCreditAmount())
                            .debitAmount(pt.getDebitAmount())
                            .balance(pt.getBalance())
                            .matchStatus(BankTransaction.MatchStatus.UNMATCHED)
                            .build())
                    .toList();

            transactionRepository.saveAll(transactions);

            // Mark statement as completed
            saved.setUploadStatus(BankStatement.UploadStatus.COMPLETED);
            saved.setProcessedAt(Instant.now());
            statementRepository.save(saved);

            log.info("Statement {} parsed: {} transactions for society {}",
                    saved.getId(), transactions.size(), societyId);

            // Run matching engine asynchronously in same transaction
            matchingService.matchTransactionsForSociety(societyId, saved.getId());

        } catch (Exception e) {
            saved.setUploadStatus(BankStatement.UploadStatus.FAILED);
            statementRepository.save(saved);
            log.error("Statement processing failed for society {}: {}", societyId, e.getMessage(), e);

            if (e instanceof SocietyLedgerException sle) throw sle;
            throw new SocietyLedgerException(
                    "Statement processing failed: " + e.getMessage(),
                    "STATEMENT_PROCESSING_FAILED",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<BankStatementResponse> getStatements(Long societyId, Pageable pageable) {
        return PageResponse.from(
                statementRepository.findBySocietyIdOrderByUploadedAtDesc(societyId, pageable)
                        .map(this::mapToResponse)
        );
    }

    @Transactional(readOnly = true)
    public BankStatementResponse getStatementById(Long societyId, Long statementId) {
        return statementRepository.findByIdAndSocietyId(statementId, societyId)
                .map(this::mapToResponse)
                .orElseThrow(() -> SocietyLedgerException.notFound("BankStatement", statementId));
    }

    private String saveFile(Long societyId, MultipartFile file) {
        try {
            Path dir = Paths.get(UPLOAD_BASE_DIR + societyId + "/");
            Files.createDirectories(dir);
            String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = dir.resolve(uniqueName);
            file.transferTo(filePath.toFile());
            return filePath.toString();
        } catch (IOException e) {
            throw new SocietyLedgerException(
                    "Failed to store uploaded file.",
                    "FILE_STORAGE_ERROR",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private BankStatementResponse mapToResponse(BankStatement s) {
        return BankStatementResponse.builder()
                .id(s.getId())
                .societyId(s.getSocietyId())
                .bankName(s.getBankName())
                .statementMonth(s.getStatementMonth())
                .statementYear(s.getStatementYear())
                .fileName(s.getFileName())
                .uploadStatus(s.getUploadStatus().name())
                .uploadedBy(s.getUploadedBy())
                .uploadedAt(s.getUploadedAt())
                .processedAt(s.getProcessedAt())
                .transactionCount(s.getTransactions() != null ? s.getTransactions().size() : 0)
                .build();
    }
}
