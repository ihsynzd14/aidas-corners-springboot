package com.aidascorner.featureOrder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DateUtil {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    
    /**
     * Format a LocalDate to a string in the format "dd.MM.yyyy"
     */
    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }
    
    /**
     * Parse a string in the format "dd.MM.yyyy" to a LocalDate
     */
    public static LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }
    
    /**
     * Get a list of date strings in the format "dd.MM.yyyy" between startDate and endDate, inclusive
     */
    public static List<String> getDateStringsInRange(LocalDate startDate, LocalDate endDate) {
        List<String> dateStrings = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            dateStrings.add(formatDate(currentDate));
            currentDate = currentDate.plusDays(1);
        }
        
        return dateStrings;
    }
}