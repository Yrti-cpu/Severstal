package org.yrti.severstal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yrti.severstal.dao.RollRepository;
import org.yrti.severstal.dto.RollCreateRequest;
import org.yrti.severstal.dto.RollFilterRequest;
import org.yrti.severstal.dto.RollStatisticsResponse;
import org.yrti.severstal.exception.RollNotFoundException;
import org.yrti.severstal.model.Roll;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RollService {

    private static final Logger log = LoggerFactory.getLogger(RollService.class);

    private final RollRepository rollRepository;

    public RollService(RollRepository rollRepository) {
        this.rollRepository = rollRepository;
    }

    @Transactional
    public Roll createRoll(RollCreateRequest request) {
        if (request.getLength() == null || request.getWeight() == null) {
            throw new IllegalArgumentException("Длина и вес обязательны");
        }
        Roll roll = new Roll(request.getLength(), request.getWeight(), LocalDateTime.now(ZoneId.of("Europe/Moscow")));
        Roll saved = rollRepository.save(roll);
        log.debug("Создан рулон: id={}, length={}, weight={}", saved.getId(), saved.getLength(), saved.getWeight());
        return saved;
    }

    @Transactional
    public Roll deleteRoll(Long id) {
        Roll roll = rollRepository.findById(id)
                .orElseThrow(() -> new RollNotFoundException("Рулон с id " + id + " не найден"));

        if (roll.getDeleteDate() != null) {
            log.warn("Попытка повторного удаления рулона id={}", id);
            return roll;
        }

        roll.setDeleteDate(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
        Roll saved = rollRepository.save(roll);
        log.debug("Рулон id={} помечен как удаленный", id);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Roll> getRolls(RollFilterRequest filter) {
        return rollRepository.findAll(createSpecification(filter));
    }

    private Specification<Roll> createSpecification(RollFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getIdMin() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("id"), filter.getIdMin()));
            if (filter.getIdMax() != null) predicates.add(cb.lessThanOrEqualTo(root.get("id"), filter.getIdMax()));

            if (filter.getLengthMin() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("length"), filter.getLengthMin()));
            if (filter.getLengthMax() != null) predicates.add(cb.lessThanOrEqualTo(root.get("length"), filter.getLengthMax()));

            if (filter.getWeightMin() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("weight"), filter.getWeightMin()));
            if (filter.getWeightMax() != null) predicates.add(cb.lessThanOrEqualTo(root.get("weight"), filter.getWeightMax()));

            if (filter.getAddDateMin() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("addDate"), filter.getAddDateMin()));
            if (filter.getAddDateMax() != null) predicates.add(cb.lessThanOrEqualTo(root.get("addDate"), filter.getAddDateMax()));

            if (filter.getDeleteDateMin() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("deleteDate"), filter.getDeleteDateMin()));
            if (filter.getDeleteDateMax() != null) predicates.add(cb.lessThanOrEqualTo(root.get("deleteDate"), filter.getDeleteDateMax()));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional(readOnly = true)
    public RollStatisticsResponse getStatistics(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Дата начала не может быть после даты окончания");
        }

        // рулоны, которые были на складе хотя бы в какой-то момент периода
        Specification<Roll> wasOnStockSpec = (root, query, cb) -> {
            Predicate addedBeforeEnd = cb.lessThanOrEqualTo(root.get("addDate"), end);
            Predicate notDeletedYet = cb.isNull(root.get("deleteDate"));
            Predicate deletedAfterStart = cb.greaterThanOrEqualTo(root.get("deleteDate"), start);
            return cb.and(addedBeforeEnd, cb.or(notDeletedYet, deletedAfterStart));
        };

        List<Roll> activeInRange = rollRepository.findAll(wasOnStockSpec);

        List<Roll> deletedInPeriod = activeInRange.stream()
                .filter(r -> r.getDeleteDate() != null && !r.getDeleteDate().isBefore(start) && !r.getDeleteDate().isAfter(end))
                .toList();

        RollStatisticsResponse stats = new RollStatisticsResponse();

        // количество добавленных
        stats.setAddedCount(activeInRange.stream()
                .filter(r -> !r.getAddDate().isBefore(start) && !r.getAddDate().isAfter(end))
                .count());

        // количество удаленных
        stats.setDeletedCount(deletedInPeriod.size());

        if (!activeInRange.isEmpty()) {
            // средняя длина и вес рулонов, находившихся на складе в этот период
            stats.setAverageLength(activeInRange.stream().mapToDouble(r -> r.getLength().doubleValue()).average().orElse(0.0));
            stats.setAverageWeight(activeInRange.stream().mapToDouble(r -> r.getWeight().doubleValue()).average().orElse(0.0));

            //макс и мин длина и вес
            stats.setMinLength(activeInRange.stream().map(Roll::getLength).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
            stats.setMaxLength(activeInRange.stream().map(Roll::getLength).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
            stats.setMinWeight(activeInRange.stream().map(Roll::getWeight).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
            stats.setMaxWeight(activeInRange.stream().map(Roll::getWeight).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));

            // суммарный вес (всех рулонов, находившихся на складе за период)
            stats.setTotalWeight(activeInRange.stream().map(Roll::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add));
        }

        // макс и мин промежуток между добавлением и удалением (для тех, кто был удален в период)
        if (!deletedInPeriod.isEmpty()) {
            List<Long> lifeSpans = deletedInPeriod.stream()
                    .map(r -> Duration.between(r.getAddDate(), r.getDeleteDate()).getSeconds())
                    .toList();
            stats.setMinLifeSpanSeconds(Collections.min(lifeSpans));
            stats.setMaxLifeSpanSeconds(Collections.max(lifeSpans));
        }

        // дни с мин/макс количеством и весом
        calculateDailyStats(stats, activeInRange, start, end);

        return stats;
    }

    private void calculateDailyStats(RollStatisticsResponse stats, List<Roll> rolls, LocalDateTime start, LocalDateTime end) {
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        Map<LocalDate, Long> dailyCount = new HashMap<>();
        Map<LocalDate, BigDecimal> dailyWeight = new HashMap<>();

        ZoneId zoneId = ZoneId.of("Europe/Moscow");

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

            ZonedDateTime dayStartZoned = date.atStartOfDay(zoneId);
            ZonedDateTime dayEndZoned = date.atTime(LocalTime.MAX).atZone(zoneId);

            LocalDateTime dayStart = dayStartZoned.toLocalDateTime();
            LocalDateTime dayEnd = dayEndZoned.toLocalDateTime();

            long count = 0;
            BigDecimal weight = BigDecimal.ZERO;

            for (Roll r : rolls) {
                // был ли рулон на складе в этот конкретный день?
                if (!r.getAddDate().isAfter(dayEnd) && (r.getDeleteDate() == null || !r.getDeleteDate().isBefore(dayStart))) {
                    count++;
                    weight = weight.add(r.getWeight());
                }
            }
            dailyCount.put(date, count);
            dailyWeight.put(date, weight);
        }

        if (!dailyCount.isEmpty()) {
            Optional<Map.Entry<LocalDate, Long>> minCountEntry = dailyCount.entrySet().stream()
                    .filter(e -> e.getValue() > 0)
                    .min(Map.Entry.comparingByValue());

            Optional<Map.Entry<LocalDate, Long>> maxCountEntry = dailyCount.entrySet().stream()
                    .filter(e -> e.getValue() > 0)
                    .max(Map.Entry.comparingByValue());

            Optional<Map.Entry<LocalDate, BigDecimal>> minWeightEntry = dailyWeight.entrySet().stream()
                    .filter(e -> e.getValue().compareTo(BigDecimal.ZERO) > 0)
                    .min(Map.Entry.comparingByValue());

            Optional<Map.Entry<LocalDate, BigDecimal>> maxWeightEntry = dailyWeight.entrySet().stream()
                    .filter(e -> e.getValue().compareTo(BigDecimal.ZERO) > 0)
                    .max(Map.Entry.comparingByValue());

            minCountEntry.ifPresent(entry -> stats.setDayWithMinCount(entry.getKey()));
            maxCountEntry.ifPresent(entry -> stats.setDayWithMaxCount(entry.getKey()));
            minWeightEntry.ifPresent(entry -> stats.setDayWithMinWeight(entry.getKey()));
            maxWeightEntry.ifPresent(entry -> stats.setDayWithMaxWeight(entry.getKey()));
        }
    }
}
