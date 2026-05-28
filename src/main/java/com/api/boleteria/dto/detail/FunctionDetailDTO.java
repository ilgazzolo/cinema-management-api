package com.api.boleteria.dto.detail;

import java.time.LocalDate;
import java.time.LocalTime;

public record FunctionDetailDTO(
        Long id,
        LocalDate date,
        LocalTime time,
        Long cinemaId,
        String cinemaName,
        Long movieId,
        String movieName,
        Integer availableCapacity,
        Integer runTime,
        Double unitPrice
) {}
