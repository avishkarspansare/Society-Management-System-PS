package com.societyledger.statement.strategy;

import com.societyledger.statement.entity.BankStatement;
import com.societyledger.statement.entity.BankTransaction;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * Strategy interface for bank statement parsers.
 * Each bank implementation is registered as a Spring bean named "<BANK_CODE>_PARSER".
 */
public interface StatementParserStrategy {
    List<BankTransaction> parse(BankStatement statement, MultipartFile file);
}
