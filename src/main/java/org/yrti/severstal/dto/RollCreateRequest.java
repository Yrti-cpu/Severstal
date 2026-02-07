package org.yrti.severstal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class RollCreateRequest {

    @NotNull(message = "Длина обязательна")
    @DecimalMin(value = "0.0", inclusive = false, message = "Длина должна быть больше 0")
    @Digits(integer = 6, fraction = 3, message = "Длина: максимум 6 цифр до и 3 после запятой")
    private BigDecimal length;

    @NotNull(message = "Вес обязателен")
    @DecimalMin(value = "0.0", inclusive = false, message = "Вес должен быть больше 0")
    @Digits(integer = 6, fraction = 3, message = "Вес: максимум 6 цифр до и 3 после запятой")
    private BigDecimal weight;

    public RollCreateRequest() {
    }

    public BigDecimal getLength() {
        return length;
    }

    public void setLength(BigDecimal length) {
        this.length = length;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }
}
