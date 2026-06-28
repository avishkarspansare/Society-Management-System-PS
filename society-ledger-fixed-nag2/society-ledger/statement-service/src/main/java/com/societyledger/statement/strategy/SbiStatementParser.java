package com.societyledger.statement.strategy;

import com.societyledger.statement.dto.ParsedTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/**
 * State Bank of India statement parser.
 * Phase 2 implementation — stub for now.
 *
 * SBI CSV format differs from BoB:
 * Txn Date,Value Date,Description,Ref No./Cheque No.,Branch Code,Debit,Credit,Balance
 */
@Slf4j
@Component
public class SbiStatementParser implements StatementParser {

    @Override
    public String getBankCode() {
        return "SBI";
    }

    @Override
    public List<ParsedTransaction> parse(InputStream inputStream, String fileName) {
        throw new UnsupportedOperationException(
                "SBI statement parsing is not yet supported. Coming in Phase 2."
        );
    }
}
