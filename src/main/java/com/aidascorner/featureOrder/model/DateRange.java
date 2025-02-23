package com.aidascorner.featureOrder.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateRange {
    private LocalDate starDate;
    private LocalDate endDate;

}
