package com.societyledger.statement.strategy;

import com.societyledger.common.exception.SocietyLedgerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("StatementParserFactory Tests")
class StatementParserFactoryTest {

    private StatementParserFactory factory;

    @BeforeEach
    void setUp() {
        factory = new StatementParserFactory(List.of(
                new BobStatementParser(),
                new SbiStatementParser(),
                new HdfcStatementParser()
        ));
    }

    @Test
    @DisplayName("Returns BOB parser for BOB bank code")
    void testGetBobParser() {
        StatementParser parser = factory.getParser("BOB");
        assertThat(parser).isInstanceOf(BobStatementParser.class);
    }

    @Test
    @DisplayName("Bank code lookup is case-insensitive")
    void testCaseInsensitiveLookup() {
        assertThat(factory.getParser("bob")).isInstanceOf(BobStatementParser.class);
        assertThat(factory.getParser("Bob")).isInstanceOf(BobStatementParser.class);
        assertThat(factory.getParser("BOB")).isInstanceOf(BobStatementParser.class);
    }

    @Test
    @DisplayName("Throws SocietyLedgerException for unsupported bank")
    void testUnsupportedBankThrows() {
        assertThatThrownBy(() -> factory.getParser("AXIS"))
                .isInstanceOf(SocietyLedgerException.class)
                .hasMessageContaining("AXIS");
    }

    @Test
    @DisplayName("getSupportedBanks returns all registered banks")
    void testGetSupportedBanks() {
        List<String> banks = factory.getSupportedBanks();
        assertThat(banks).containsExactlyInAnyOrder("BOB", "SBI", "HDFC");
    }

    @Test
    @DisplayName("Factory auto-discovers parsers from injected list")
    void testAutoDiscovery() {
        // With only one parser injected
        StatementParserFactory singleFactory = new StatementParserFactory(
                List.of(new BobStatementParser())
        );
        assertThat(singleFactory.getSupportedBanks()).containsExactly("BOB");
        assertThatThrownBy(() -> singleFactory.getParser("SBI"))
                .isInstanceOf(SocietyLedgerException.class);
    }
}
