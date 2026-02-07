package org.yrti.severstal.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rolls")
public class Roll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal length;

    @Column(nullable = false)
    private BigDecimal weight;

    @Column(name = "add_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Europe/Moscow")
    private LocalDateTime addDate;

    @Column(name = "delete_date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Europe/Moscow")
    private LocalDateTime deleteDate;

    public Roll() {
    }

    public Roll(BigDecimal length, BigDecimal weight, LocalDateTime addDate) {
        this.length = length;
        this.weight = weight;
        this.addDate = addDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getAddDate() {
        return addDate;
    }

    public void setAddDate(LocalDateTime addDate) {
        this.addDate = addDate;
    }

    public LocalDateTime getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(LocalDateTime deleteDate) {
        this.deleteDate = deleteDate;
    }

    @Override
    public String toString() {
        return "Roll{" +
                "id=" + id +
                ", length=" + length +
                ", weight=" + weight +
                ", addDate=" + addDate +
                ", deleteDate=" + deleteDate +
                '}';
    }
}
