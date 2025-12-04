package com.example.community;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for validating and working with date strings.
 * All dates use yyyy-MM-dd format.
 */
public class DateValidation {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Checks if a date string matches the expected format (yyyy-MM-dd).
     *
     * @param date date string to validate
     * @return true if valid format, false otherwise
     */
    public static boolean isValidDateFormat(String date) {
        try {
            LocalDate.parse(date, DATE_FORMAT);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Checks if the start date is before or equal to the end date.
     *
     * @param startDate start date string
     * @param endDate   end date string
     * @return true if range is valid, false otherwise
     */
    public static boolean dateRangeValid(String startDate, String endDate) {
        if (!isValidDateFormat(startDate) || !isValidDateFormat(endDate)) {
            return false;
        }
        return !LocalDate.parse(startDate, DATE_FORMAT)
                .isAfter(LocalDate.parse(endDate, DATE_FORMAT));
    }

    /**
     * Checks if a date falls within the specified range.
     *
     * @param date      date to check
     * @param startDate start of range
     * @param endDate   end of range
     * @return true if date is within range, false otherwise
     */
    public static boolean isInDateRange(String date, String startDate, String endDate) {
        if (!isValidDateFormat(date)
                || !isValidDateFormat(startDate) || !isValidDateFormat(endDate)) {
            return false;
        }
        LocalDate d = LocalDate.parse(date, DATE_FORMAT);
        LocalDate start = LocalDate.parse(startDate, DATE_FORMAT);
        LocalDate end = LocalDate.parse(endDate, DATE_FORMAT);
        return !d.isBefore(start) && !d.isAfter(end);
    }

    /**
     * Returns today's date in yyyy-MM-dd format.
     *
     * @return current date string
     */
    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMAT);
    }
}
