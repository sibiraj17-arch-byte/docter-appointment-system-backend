package com.healthcare.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class DateUtils {
    public static final LocalTime WORK_START = LocalTime.of(9, 0);
    public static final LocalTime WORK_END = LocalTime.of(17, 0);

    public static boolean isWithinWorkingHours(LocalTime time) {
        return !time.isBefore(WORK_START) && !time.isAfter(WORK_END);
    }

    public static boolean isWorkingDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    public static boolean isOneHourSlot(LocalTime start, LocalTime end) {
        return end.minusHours(1).equals(start);
    }

    public static String generateAppointmentCode() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);
        return "APPT" + timestamp + random;
    }

    public static String generateTransactionId() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 10000);
        return "TXN" + timestamp + random;
    }
} 
