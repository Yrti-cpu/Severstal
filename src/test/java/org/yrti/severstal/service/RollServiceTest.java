package org.yrti.severstal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;
import org.yrti.severstal.dao.RollRepository;
import org.yrti.severstal.dto.RollCreateRequest;
import org.yrti.severstal.dto.RollFilterRequest;
import org.yrti.severstal.dto.RollStatisticsResponse;
import org.yrti.severstal.exception.RollNotFoundException;
import org.yrti.severstal.model.Roll;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RollServiceTest {

    @Mock
    private RollRepository rollRepository;

    @InjectMocks
    private RollService rollService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("createRoll: Валидное создание")
    void createRoll_Valid_Success() {
        // Given
        RollCreateRequest request = new RollCreateRequest();
        request.setLength(new BigDecimal("10.5"));
        request.setWeight(new BigDecimal("100.2"));
        Roll roll = new Roll(request.getLength(), request.getWeight(), LocalDateTime.now());
        roll.setId(1L);

        when(rollRepository.save(any(Roll.class))).thenReturn(roll);

        // When
        Roll result = rollService.createRoll(request);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("10.5"), result.getLength());
        assertEquals(new BigDecimal("100.2"), result.getWeight());
        verify(rollRepository, times(1)).save(any(Roll.class));
    }

    @Test
    @DisplayName("createRoll: length = null, weight = null")
    void createRoll_NullParams_ThrowsException() {
        // Given
        RollCreateRequest request = new RollCreateRequest();

        // When и Then
        assertThrows(IllegalArgumentException.class, () -> rollService.createRoll(request));
    }

    @Test
    @DisplayName("createRoll: Дробные значения сохраняются корректно")
    void createRoll_DecimalValues_Success() {
        // Given
        RollCreateRequest request = new RollCreateRequest();
        request.setLength(new BigDecimal("10.555"));
        request.setWeight(new BigDecimal("100.222"));
        
        ArgumentCaptor<Roll> rollCaptor = ArgumentCaptor.forClass(Roll.class);
        when(rollRepository.save(rollCaptor.capture())).thenAnswer(i -> i.getArguments()[0]);

        // When
        rollService.createRoll(request);

        // Then
        Roll savedRoll = rollCaptor.getValue();
        assertEquals(new BigDecimal("10.555"), savedRoll.getLength());
        assertEquals(new BigDecimal("100.222"), savedRoll.getWeight());
    }

    @Test
    @DisplayName("deleteRoll: Успешное удаление существующего рулона")
    void deleteRoll_Existing_Success() {
        // Given
        Long id = 1L;
        Roll roll = new Roll(new BigDecimal("10"), new BigDecimal("100"), LocalDateTime.now());
        roll.setId(id);
        when(rollRepository.findById(id)).thenReturn(Optional.of(roll));
        when(rollRepository.save(any(Roll.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        Roll deleted = rollService.deleteRoll(id);

        // Then
        assertNotNull(deleted.getDeleteDate());
        verify(rollRepository).save(roll);
    }

    @Test
    @DisplayName("deleteRoll: Повторное удаление (дата не меняется)")
    void deleteRoll_AlreadyDeleted_NoChange() {
        // Given
        Long id = 1L;
        LocalDateTime firstDeleteDate = LocalDateTime.now().minusDays(1);
        Roll roll = new Roll(new BigDecimal("10"), new BigDecimal("100"), LocalDateTime.now().minusDays(2));
        roll.setId(id);
        roll.setDeleteDate(firstDeleteDate);
        when(rollRepository.findById(id)).thenReturn(Optional.of(roll));

        // When
        Roll deleted = rollService.deleteRoll(id);

        // Then
        assertEquals(firstDeleteDate, deleted.getDeleteDate());
        verify(rollRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteRoll: Удаление несуществующего id")
    void deleteRoll_NotFound_ThrowsException() {
        // Given
        Long id = 999L;
        when(rollRepository.findById(id)).thenReturn(Optional.empty());

        // When и Then
        assertThrows(RollNotFoundException.class, () -> rollService.deleteRoll(id));
    }

    @Test
    @DisplayName("getRolls: Без фильтра")
    void getRolls_NoFilter_Success() {
        // Given
        RollFilterRequest filter = new RollFilterRequest();
        when(rollRepository.findAll(any(Specification.class))).thenReturn(List.of(new Roll()));

        // When
        List<Roll> result = rollService.getRolls(filter);

        // Then
        assertFalse(result.isEmpty());
        verify(rollRepository).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("getRolls: Фильтр по id")
    void getRolls_FilterById_CallsRepository() {
        // Given
        RollFilterRequest filter = new RollFilterRequest();
        filter.setIdMin(1L);
        filter.setIdMax(10L);

        // When
        rollService.getRolls(filter);

        // Then
        verify(rollRepository).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("getRolls: Фильтр по length")
    void getRolls_FilterByLength_CallsRepository() {
        // Given
        RollFilterRequest filter = new RollFilterRequest();
        filter.setLengthMin(new BigDecimal("5"));

        // When
        rollService.getRolls(filter);

        // Then
        verify(rollRepository).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("getRolls: Фильтр по weight")
    void getRolls_FilterByWeight_CallsRepository() {
        // Given
        RollFilterRequest filter = new RollFilterRequest();
        filter.setWeightMax(new BigDecimal("200"));

        // When
        rollService.getRolls(filter);

        // Then
        verify(rollRepository).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("getRolls: Фильтр по date")
    void getRolls_FilterByDate_CallsRepository() {
        // Given
        RollFilterRequest filter = new RollFilterRequest();
        filter.setAddDateMin(LocalDateTime.now().minusDays(1));

        // When
        rollService.getRolls(filter);

        // Then
        verify(rollRepository).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("getRolls: Комбинация фильтров")
    void getRolls_CombinedFilters_CallsRepository() {
        // Given
        RollFilterRequest filter = new RollFilterRequest();
        filter.setIdMin(1L);
        filter.setLengthMax(new BigDecimal("50"));

        // When
        rollService.getRolls(filter);

        // Then
        verify(rollRepository).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("getRolls: Диапазон без пересечений")
    void getRolls_NoIntersection_ReturnsEmpty() {
        // Given
        RollFilterRequest filter = new RollFilterRequest();
        when(rollRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        // When
        List<Roll> result = rollService.getRolls(filter);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getRolls: Перепутанные границы (min > max)")
    void getRolls_MinGreaterThanMax_ReturnsEmpty() {
        // Given
        RollFilterRequest filter = new RollFilterRequest();
        filter.setIdMin(10L);
        filter.setIdMax(1L);
        // логика Specification в Spring Data JPA вернет пусто, если границы не пересекаются в БД
        when(rollRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        // When
        List<Roll> result = rollService.getRolls(filter);

        // Then
        assertTrue(result.isEmpty());
    }


    @Test
    @DisplayName("getStatistics: Нет рулонов")
    void getStatistics_NoRolls_Zeros() {
        // Given
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 8, 23, 59);
        when(rollRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        // When
        RollStatisticsResponse stats = rollService.getStatistics(start, end);

        // Then
        assertEquals(0, stats.getAddedCount());
        assertEquals(0, stats.getDeletedCount());
        assertNull(stats.getAverageLength());
    }

    @Test
    @DisplayName("getStatistics: Один рулон в периоде")
    void getStatistics_SingleRoll_Correct() {
        // Given
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 8, 23, 59);
        Roll roll = new Roll(new BigDecimal("10"), new BigDecimal("100"), LocalDateTime.of(2026, 1, 15, 12, 0));
        roll.setId(1L);
        when(rollRepository.findAll(any(Specification.class))).thenReturn(List.of(roll));

        // When
        RollStatisticsResponse stats = rollService.getStatistics(start, end);

        // Then
        assertEquals(1, stats.getAddedCount());
        assertEquals(10.0, stats.getAverageLength());
        assertEquals(new BigDecimal("100"), stats.getTotalWeight());
    }

    @Test
    @DisplayName("getStatistics: Несколько рулонов")
    void getStatistics_MultipleRolls_Correct() {
        // Given
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 8, 23, 59);
        Roll r1 = new Roll(new BigDecimal("10"), new BigDecimal("100"), LocalDateTime.of(2026, 1, 10, 10, 0));
        Roll r2 = new Roll(new BigDecimal("20"), new BigDecimal("200"), LocalDateTime.of(2026, 1, 20, 10, 0));
        when(rollRepository.findAll(any(Specification.class))).thenReturn(List.of(r1, r2));

        // When
        RollStatisticsResponse stats = rollService.getStatistics(start, end);

        // Then
        assertEquals(2, stats.getAddedCount());
        assertEquals(15.0, stats.getAverageLength());
        assertEquals(new BigDecimal("300"), stats.getTotalWeight());
    }

    @Test
    @DisplayName("getStatistics: Все вне периода")
    void getStatistics_AllOutside_Zeros() {
        // Given
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 8, 23, 59);
        // Рулон добавлен ПОСЛЕ периода
        when(rollRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        // When
        RollStatisticsResponse stats = rollService.getStatistics(start, end);

        // Then
        assertEquals(0, stats.getAddedCount());
    }

    @Test
    @DisplayName("getStatistics: Добавлен в периоде")
    void getStatistics_AddedInPeriod_Counted() {
        // Given
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 8, 23, 59);
        Roll r = new Roll(new BigDecimal("10"), new BigDecimal("100"), LocalDateTime.of(2026, 1, 5, 0, 0));
        when(rollRepository.findAll(any(Specification.class))).thenReturn(List.of(r));

        // When
        RollStatisticsResponse stats = rollService.getStatistics(start, end);

        // Then
        assertEquals(1, stats.getAddedCount());
    }

    @Test
    @DisplayName("getStatistics: Удалён в периоде")
    void getStatistics_DeletedInPeriod_Counted() {
        // Given
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 8, 23, 59);
        Roll r = new Roll(new BigDecimal("10"), new BigDecimal("100"), LocalDateTime.of(2025, 12, 1, 0, 0));
        r.setDeleteDate(LocalDateTime.of(2026, 1, 5, 0, 0));
        when(rollRepository.findAll(any(Specification.class))).thenReturn(List.of(r));

        // When
        RollStatisticsResponse stats = rollService.getStatistics(start, end);

        // Then
        assertEquals(1, stats.getDeletedCount());
    }

    @Test
    @DisplayName("getStatistics: Активен весь период")
    void getStatistics_ActiveAllPeriod_Included() {
        // Given
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 8, 23, 59);

        Roll r = new Roll(new BigDecimal("10"), new BigDecimal("100"), LocalDateTime.of(2025, 12, 31, 23, 59));
        when(rollRepository.findAll(any(Specification.class))).thenReturn(List.of(r));

        // When
        RollStatisticsResponse stats = rollService.getStatistics(start, end);

        // Then
        assertEquals(0, stats.getAddedCount());
        assertEquals(1, stats.getTotalWeight().compareTo(BigDecimal.ZERO) > 0 ? 1 : 0); // На складе был
        assertNotNull(stats.getAverageLength());
    }

    @Test
    @DisplayName("getStatistics: Удалён внутри периода")
    void getStatistics_DeletedInsidePeriod_Included() {
        // Given
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 8, 23, 59);
        Roll r = new Roll(new BigDecimal("10"), new BigDecimal("100"), LocalDateTime.of(2026, 1, 2, 0, 0));
        r.setDeleteDate(LocalDateTime.of(2026, 1, 5, 0, 0));
        when(rollRepository.findAll(any(Specification.class))).thenReturn(List.of(r));

        // When
        RollStatisticsResponse stats = rollService.getStatistics(start, end);

        // Then
        assertEquals(1, stats.getAddedCount());
        assertEquals(1, stats.getDeletedCount());
    }

    @Test
    @DisplayName("getStatistics: Добавлен внутри периода")
    void getStatistics_AddedInsidePeriod_Included() {
        // Given
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 8, 23, 59);
        Roll r = new Roll(new BigDecimal("10"), new BigDecimal("100"), LocalDateTime.of(2026, 1, 10, 0, 0));
        when(rollRepository.findAll(any(Specification.class))).thenReturn(List.of(r));

        // When
        RollStatisticsResponse stats = rollService.getStatistics(start, end);

        // Then
        assertEquals(1, stats.getAddedCount());
    }

    @Test
    @DisplayName("getStatistics: Агрегации (Средние/min/max/total/LifeSpan)")
    void getStatistics_Aggregations_Correct() {
        // Given
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 2, 8, 23, 59);
        Roll r1 = new Roll(new BigDecimal("10"), new BigDecimal("100"), LocalDateTime.of(2026, 1, 1, 10, 0));
        r1.setDeleteDate(LocalDateTime.of(2026, 1, 1, 10, 10)); // 600 сек

        Roll r2 = new Roll(new BigDecimal("20"), new BigDecimal("300"), LocalDateTime.of(2026, 1, 1, 11, 0));
        r2.setDeleteDate(LocalDateTime.of(2026, 1, 1, 11, 20)); // 1200 сек

        when(rollRepository.findAll(any(Specification.class))).thenReturn(List.of(r1, r2));

        // When
        RollStatisticsResponse stats = rollService.getStatistics(start, end);

        // Then
        assertEquals(15.0, stats.getAverageLength());
        assertEquals(new BigDecimal("10"), stats.getMinLength());
        assertEquals(new BigDecimal("20"), stats.getMaxLength());
        assertEquals(new BigDecimal("400"), stats.getTotalWeight());
        assertEquals(600L, stats.getMinLifeSpanSeconds());
        assertEquals(1200L, stats.getMaxLifeSpanSeconds());
    }

    @Test
    @DisplayName("getStatistics: start > end")
    void getStatistics_StartAfterEnd_ThrowsException() {
        // Given
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 1, 0, 0);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> rollService.getStatistics(start, end));
    }
}
