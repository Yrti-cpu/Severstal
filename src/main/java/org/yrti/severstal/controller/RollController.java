package org.yrti.severstal.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yrti.severstal.dto.RollCreateRequest;
import org.yrti.severstal.dto.RollFilterRequest;
import org.yrti.severstal.dto.RollStatisticsResponse;
import org.yrti.severstal.model.Roll;
import org.yrti.severstal.service.RollService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rolls")
public class RollController {

    private final RollService rollService;

    public RollController(RollService rollService) {
        this.rollService = rollService;
    }

    @PostMapping
    public ResponseEntity<Roll> createRoll(@Valid @RequestBody RollCreateRequest request) {
        return ResponseEntity.ok(rollService.createRoll(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Roll> deleteRoll(@Valid @PathVariable Long id) {
        return ResponseEntity.ok(rollService.deleteRoll(id));
    }

    @GetMapping
    public ResponseEntity<List<Roll>> getRolls(@Valid RollFilterRequest filter) {
        return ResponseEntity.ok(rollService.getRolls(filter));
    }

    @GetMapping("/statistics")
    public ResponseEntity<RollStatisticsResponse> getStatistics(
            @Valid
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(rollService.getStatistics(start, end));
    }
}
