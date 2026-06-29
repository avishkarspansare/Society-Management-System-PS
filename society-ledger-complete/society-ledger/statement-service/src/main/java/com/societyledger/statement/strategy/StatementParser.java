package com.societyledger.statement.strategy;

import com.societyledger.statement.dto.ParsedTransaction;

import java.io.InputStream;
import java.util.List;

/**
 * Strategy interface for parsing bank statement files.
 *
 * Each bank has its own format (CSV columns, Excel structure, date formats).
 * Adding a new bank = implementing this interface and registering in StatementParserFactory.
 *
 * Supported: BOB
 * Planned:   SBI, HDFC, ICICI, AXIS
 */
public interface StatementParser {

    /**
     * Returns the bank identifier this parser handles.
     * e.g. "BOB", "SBI", "HDFC"
     */
    String getBankCode();

    /**
     * Parse a bank statement file and return a list of raw transactions.
     *
     * @param inputStream the uploaded file input stream
     * @param fileName    original file name (used to detect format .csv/.xlsx)
     * @return list of parsed transaction DTOs
     */
    List<ParsedTransaction> parse(InputStream inputStream, String fileName);

    /**
     * Validate that the uploaded file is a valid statement for this bank.
     * Should throw IllegalArgumentException with a clear message if invalid.
     */
    default void validate(InputStream inputStream, String fileName) {
        // default: no-op; override for strict validation
    }
}
