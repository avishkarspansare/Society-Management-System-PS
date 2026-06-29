package com.societyledger.society.service;

import com.societyledger.common.exception.SocietyLedgerException;
import com.societyledger.society.entity.Flat;
import com.societyledger.society.entity.Wing;
import com.societyledger.society.repository.FlatRepository;
import com.societyledger.society.repository.WingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FlatService Unit Tests")
class FlatServiceTest {

    @Mock FlatRepository flatRepository;
    @Mock WingRepository wingRepository;
    @InjectMocks FlatService flatService;

    private Wing wingA;
    private Flat flat101;

    @BeforeEach
    void setUp() {
        wingA = Wing.builder()
                .id(1L).societyId(10L).wingName("A").build();

        flat101 = Flat.builder()
                .id(1L).societyId(10L).wing(wingA)
                .flatNumber("A-101").floorNumber(1)
                .paymentReferenceCode("SOC-A-101")
                .isOccupied(true)
                .build();
    }

    @Test
    @DisplayName("getFlatsBySociety returns flats sorted by flat number")
    void getFlatsBySociety_returnsFlats() {
        when(flatRepository.findBySocietyId(10L)).thenReturn(List.of(flat101));

        var result = flatService.getFlatsBySociety(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFlatNumber()).isEqualTo("A-101");
        assertThat(result.get(0).getPaymentReferenceCode()).isEqualTo("SOC-A-101");
    }

    @Test
    @DisplayName("getFlatById throws NOT_FOUND for unknown flat")
    void getFlatById_throwsForUnknown() {
        when(flatRepository.findByIdAndSocietyId(eq(99L), eq(10L)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> flatService.getFlatById(10L, 99L))
                .isInstanceOf(SocietyLedgerException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("getFlatById returns correct flat for valid id")
    void getFlatById_returnsFlat() {
        when(flatRepository.findByIdAndSocietyId(eq(1L), eq(10L)))
                .thenReturn(Optional.of(flat101));

        var result = flatService.getFlatById(10L, 1L);

        assertThat(result.getFlatNumber()).isEqualTo("A-101");
        assertThat(result.isOccupied()).isTrue();
    }

    @Test
    @DisplayName("duplicate paymentReferenceCode throws CONFLICT")
    void createFlat_duplicateRefCode_throwsConflict() {
        when(wingRepository.findByIdAndSocietyId(eq(1L), eq(10L)))
                .thenReturn(Optional.of(wingA));
        when(flatRepository.existsBySocietyIdAndPaymentReferenceCode(eq(10L), eq("SOC-A-101")))
                .thenReturn(true);

        var req = new com.societyledger.society.dto.request.CreateFlatRequest();
        req.setWingId(1L);
        req.setFlatNumber("A-102");
        req.setPaymentReferenceCode("SOC-A-101");

        assertThatThrownBy(() -> flatService.createFlat(10L, req))
                .isInstanceOf(SocietyLedgerException.class)
                .hasMessageContaining("already exists");
    }
}
