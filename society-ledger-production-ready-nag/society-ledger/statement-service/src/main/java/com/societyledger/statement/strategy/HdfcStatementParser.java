package com.societyledger.statement.strategy;

import com.societyledger.statement.dto.ParsedTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/**
 * HDFC Bank statement parser.
 * Phase 2 implementation — stub for now.
 */
@Slf4j
@Component
public class HdfcStatementParser implements StatementParser {

    @Override
    public String getBankCode() {
        return "HDFC";
    }

    @Override
    public List<ParsedTransaction> parse(InputStream inputStream, String fileName) {
        throw new UnsupportedOperationException(
                "HDFC statement parsing is not yet supported. Coming in Phase 2."
        );
    }
}
