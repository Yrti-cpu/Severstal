package org.yrti.severstal.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RollFilterRequest {

    @Min(value = 1, message = "ID должен быть положительным числом")
    private Long idMin;
    @Min(value = 1, message = "ID должен быть положительным числом")
    private Long idMax;

    @DecimalMin(value = "0.0", inclusive = false, message = "Длина должна быть больше 0")
    @Digits(integer = 6, fraction = 3, message = "Длина: максимум 6 цифр до и 3 после запятой")
    private BigDecimal lengthMin;

    @DecimalMin(value = "0.0", inclusive = false, message = "Длина должна быть больше 0")
    @Digits(integer = 6, fraction = 3, message = "Длина: максимум 6 цифр до и 3 после запятой")
    private BigDecimal lengthMax;

    @DecimalMin(value = "0.0", inclusive = false, message = "Длина должна быть больше 0")
    @Digits(integer = 6, fraction = 3, message = "Длина: максимум 6 цифр до и 3 после запятой")
    private BigDecimal weightMin;

    @DecimalMin(value = "0.0", inclusive = false, message = "Длина должна быть больше 0")
    @Digits(integer = 6, fraction = 3, message = "Длина: максимум 6 цифр до и 3 после запятой")
    private BigDecimal weightMax;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime addDateMin;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime addDateMax;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime deleteDateMin;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime deleteDateMax;

    public RollFilterRequest() {
    }

    @AssertTrue(message = "Минимальное значение ID не может быть больше максимального")
    public boolean isIdRangeValid() {
        return idMin == null || idMax == null || idMin <= idMax;
    }

    @AssertTrue(message = "Минимальная длина не может быть больше максимальной")
    public boolean isLengthRangeValid() {
        return lengthMin == null || lengthMax == null ||
                lengthMin.compareTo(lengthMax) <= 0;
    }

    @AssertTrue(message = "Минимальный вес не может быть больше максимального")
    public boolean isWeightRangeValid() {
        return weightMin == null || weightMax == null ||
                weightMin.compareTo(weightMax) <= 0;
    }

    @AssertTrue(message = "Начальная дата не может быть позже конечной")
    public boolean isAddDateRangeValid() {
        return addDateMin == null || addDateMax == null ||
                !addDateMin.isAfter(addDateMax);
    }

    @AssertTrue(message = "Дата удаления: начало не может быть позже конца")
    public boolean isDeleteDateRangeValid() {
        return deleteDateMin == null || deleteDateMax == null ||
                !deleteDateMin.isAfter(deleteDateMax);
    }

    @AssertTrue(message = "Дата удаления не может быть раньше даты добавления (если указаны обе)")
    public boolean isDeleteNotBeforeAdd() {
        return deleteDateMin == null || addDateMin == null ||
                !deleteDateMin.isBefore(addDateMin);
    }

    public Long getIdMin() {
        return idMin;
    }

    public void setIdMin(Long idMin) {
        this.idMin = idMin;
    }

    public Long getIdMax() {
        return idMax;
    }

    public void setIdMax(Long idMax) {
        this.idMax = idMax;
    }

    public BigDecimal getLengthMin() {
        return lengthMin;
    }

    public void setLengthMin(BigDecimal lengthMin) {
        this.lengthMin = lengthMin;
    }

    public BigDecimal getLengthMax() {
        return lengthMax;
    }

    public void setLengthMax(BigDecimal lengthMax) {
        this.lengthMax = lengthMax;
    }

    public BigDecimal getWeightMin() {
        return weightMin;
    }

    public void setWeightMin(BigDecimal weightMin) {
        this.weightMin = weightMin;
    }

    public BigDecimal getWeightMax() {
        return weightMax;
    }

    public void setWeightMax(BigDecimal weightMax) {
        this.weightMax = weightMax;
    }

    public LocalDateTime getAddDateMin() {
        return addDateMin;
    }

    public void setAddDateMin(LocalDateTime addDateMin) {
        this.addDateMin = addDateMin;
    }

    public LocalDateTime getAddDateMax() {
        return addDateMax;
    }

    public void setAddDateMax(LocalDateTime addDateMax) {
        this.addDateMax = addDateMax;
    }

    public LocalDateTime getDeleteDateMin() {
        return deleteDateMin;
    }

    public void setDeleteDateMin(LocalDateTime deleteDateMin) {
        this.deleteDateMin = deleteDateMin;
    }

    public LocalDateTime getDeleteDateMax() {
        return deleteDateMax;
    }

    public void setDeleteDateMax(LocalDateTime deleteDateMax) {
        this.deleteDateMax = deleteDateMax;
    }
}
