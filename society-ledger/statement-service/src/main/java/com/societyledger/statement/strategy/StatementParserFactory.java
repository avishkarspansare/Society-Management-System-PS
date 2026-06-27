package com.societyledger.statement.strategy;

import com.societyledger.common.exception.SocietyLedgerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory that resolves the correct StatementParser implementation for a given bank code.
 *
 * All StatementParser beans are auto-injected via Spring — adding a new bank parser
 * requires only: implement StatementParser, annotate with @Component.
 * No changes needed here.
 *
 * Uses Java Streams to build the registry map from the injected list.
 */
@Slf4j
@Component
public class StatementParserFactory {

    private final Map<String, StatementParser> parserRegistry;

    public StatementParserFactory(List<StatementParser> parsers) {
        // Build registry: bankCode → parser implementation
        this.parserRegistry = parsers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        parser -> parser.getBankCode().toUpperCase(),
                        Function.identity()
                ));

        log.info("StatementParserFactory initialized with parsers: {}",
                parserRegistry.keySet());
    }

    /**
     * Resolve the parser for a given bank code.
     *
     * @param bankCode e.g. "BOB", "SBI", "HDFC"
     * @return the matching StatementParser
     * @throws SocietyLedgerException if bank is not supported
     */
    public StatementParser getParser(String bankCode) {
        StatementParser parser = parserRegistry.get(bankCode.toUpperCase());
        if (parser == null) {
            throw new SocietyLedgerException(
                    "Bank '" + bankCode + "' is not supported yet. " +
                    "Supported banks: " + String.join(", ", parserRegistry.keySet()),
                    "UNSUPPORTED_BANK",
                    HttpStatus.BAD_REQUEST
            );
        }
        return parser;
    }

    public List<String> getSupportedBanks() {
        return List.copyOf(parserRegistry.keySet());
    }
}
