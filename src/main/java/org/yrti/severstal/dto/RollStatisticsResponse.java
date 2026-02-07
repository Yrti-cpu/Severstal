package org.yrti.severstal.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class RollStatisticsResponse {

    private long addedCount;
    private long deletedCount;
    private Double averageLength;
    private Double averageWeight;
    private BigDecimal minLength;
    private BigDecimal maxLength;
    private BigDecimal minWeight;
    private BigDecimal maxWeight;
    private BigDecimal totalWeight;
    private Long minLifeSpanSeconds;
    private Long maxLifeSpanSeconds;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate  dayWithMinCount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate  dayWithMaxCount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate  dayWithMinWeight;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate  dayWithMaxWeight;

    public RollStatisticsResponse() {
    }

    public long getAddedCount() {
        return addedCount;
    }

    public void setAddedCount(long addedCount) {
        this.addedCount = addedCount;
    }

    public long getDeletedCount() {
        return deletedCount;
    }

    public void setDeletedCount(long deletedCount) {
        this.deletedCount = deletedCount;
    }

    public Double getAverageLength() {
        return averageLength;
    }

    public void setAverageLength(Double averageLength) {
        this.averageLength = averageLength;
    }

    public Double getAverageWeight() {
        return averageWeight;
    }

    public void setAverageWeight(Double averageWeight) {
        this.averageWeight = averageWeight;
    }

    public BigDecimal getMinLength() {
        return minLength;
    }

    public void setMinLength(BigDecimal minLength) {
        this.minLength = minLength;
    }

    public BigDecimal getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(BigDecimal maxLength) {
        this.maxLength = maxLength;
    }

    public BigDecimal getMinWeight() {
        return minWeight;
    }

    public void setMinWeight(BigDecimal minWeight) {
        this.minWeight = minWeight;
    }

    public BigDecimal getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(BigDecimal maxWeight) {
        this.maxWeight = maxWeight;
    }

    public BigDecimal getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(BigDecimal totalWeight) {
        this.totalWeight = totalWeight;
    }

    public Long getMinLifeSpanSeconds() {
        return minLifeSpanSeconds;
    }

    public void setMinLifeSpanSeconds(Long minLifeSpanSeconds) {
        this.minLifeSpanSeconds = minLifeSpanSeconds;
    }

    public Long getMaxLifeSpanSeconds() {
        return maxLifeSpanSeconds;
    }

    public void setMaxLifeSpanSeconds(Long maxLifeSpanSeconds) {
        this.maxLifeSpanSeconds = maxLifeSpanSeconds;
    }

    public LocalDate  getDayWithMinCount() {
        return dayWithMinCount;
    }

    public void setDayWithMinCount(LocalDate  dayWithMinCount) {
        this.dayWithMinCount = dayWithMinCount;
    }

    public LocalDate  getDayWithMaxCount() {
        return dayWithMaxCount;
    }

    public void setDayWithMaxCount(LocalDate  dayWithMaxCount) {
        this.dayWithMaxCount = dayWithMaxCount;
    }

    public LocalDate  getDayWithMinWeight() {
        return dayWithMinWeight;
    }

    public void setDayWithMinWeight(LocalDate  dayWithMinWeight) {
        this.dayWithMinWeight = dayWithMinWeight;
    }

    public LocalDate  getDayWithMaxWeight() {
        return dayWithMaxWeight;
    }

    public void setDayWithMaxWeight(LocalDate  dayWithMaxWeight) {
        this.dayWithMaxWeight = dayWithMaxWeight;
    }
}
