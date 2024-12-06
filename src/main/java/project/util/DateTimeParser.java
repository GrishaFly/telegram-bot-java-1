package project.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTimeParser {
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{1,2})[:.](\\d{2})");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy[ HH:mm]");

    public static LocalDateTime parseUserInput(String input, ZoneId userTimeZone) {
        input = input.toLowerCase().trim();
        LocalDateTime now = LocalDateTime.now(userTimeZone);

        try {
            // Попытка разобрать стандартный формат
            return LocalDateTime.parse(input, DATE_FORMATTER);
        } catch (Exception e) {
            // Обработка естественного языка
//            if (input.startsWith("завтра")) {
//                return parseTomorrowWithTime(input, now);
//            }

            switch (input) {
                case "сегодня":
                    return now.withHour(12).withMinute(0);
                case "завтра":
                    return now.plusDays(1).withHour(12).withMinute(0);
                case "послезавтра":
                    return now.plusDays(2).withHour(12).withMinute(0);
                default:
                    // Обработка дней недели
                    DayOfWeek targetDay = parseDayOfWeek(input);
                    if (targetDay != null) {
                        return getNextDayOfWeek(now, targetDay);
                    }

                    // Обработка относительных дат
                    if (input.contains("через")) {
                        return parseRelativeDate(input, now);
                    }
            }
        }

        throw new IllegalArgumentException("Неверный формат даты. Используйте:\n" +
                "- dd/MM/yy HH:mm (например, 25/12/23 15:30)\n" +
                "- завтра в HH:mm (например, завтра в 14:32 или 14.32)\n" +
                "- сегодня, завтра, послезавтра\n" +
                "- день недели (например, понедельник)\n" +
                "- через X дней/часов/минут");
    }

    private static LocalDateTime parseTomorrowWithTime(String input, LocalDateTime now) {
        Matcher matcher = TIME_PATTERN.matcher(input);
        if (matcher.find()) {
            int hour = Integer.parseInt(matcher.group(1));
            int minute = Integer.parseInt(matcher.group(2));

            if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                return now.plusDays(1)
                        .withHour(hour)
                        .withMinute(minute)
                        .withSecond(0)
                        .withNano(0);
            }
        }
        throw new IllegalArgumentException("Неверный формат времени. Используйте: HH:mm или HH.mm (например, 14:32 или 14.32)");
    }

    private static DayOfWeek parseDayOfWeek(String input) {
        switch (input) {
            case "понедельник": return DayOfWeek.MONDAY;
            case "вторник": return DayOfWeek.TUESDAY;
            case "среда": return DayOfWeek.WEDNESDAY;
            case "четверг": return DayOfWeek.THURSDAY;
            case "пятница": return DayOfWeek.FRIDAY;
            case "суббота": return DayOfWeek.SATURDAY;
            case "воскресенье": return DayOfWeek.SUNDAY;
            default: return null;
        }
    }

    private static LocalDateTime getNextDayOfWeek(LocalDateTime now, DayOfWeek targetDay) {
        LocalDateTime next = now.with(targetDay);
        if (next.isBefore(now) || next.equals(now)) {
            next = next.plusWeeks(1);
        }
        return next.withHour(12).withMinute(0);
    }

    private static LocalDateTime parseRelativeDate(String input, LocalDateTime now) {
        String[] parts = input.split("\\s+");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Неверный формат относительной даты");
        }

        int amount = Integer.parseInt(parts[1]);
        String unit = parts[2].toLowerCase();

        switch (unit) {
            case "минут":
            case "минуты":
            case "минуту":
                return now.plusMinutes(amount);
            case "часов":
            case "часа":
            case "час":
                return now.plusHours(amount);
            case "дней":
            case "дня":
            case "день":
                return now.plusDays(amount);
            default:
                throw new IllegalArgumentException("Неподдерживаемая единица времени: " + unit);
        }
    }

    public static boolean isValidDate(String input, ZoneId userTimeZone) {
        try {
            parseUserInput(input, userTimeZone);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
