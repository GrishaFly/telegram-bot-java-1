package project.util;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;

public class TimeZoneFinder {
    private static final Map<String, String> CITY_TIMEZONES = new HashMap<>();
    private static final Map<String, List<String>> CITY_ALIASES = new HashMap<>();
    private static final int MIN_CITY_LENGTH = 3;

    static {
        // Moscow Time (UTC+3)
        addCity("москва", "Europe/Moscow", "мск", "moscow");
        addCity("санкт-петербург", "Europe/Moscow", "питер", "спб", "санкт петербург", "saint petersburg");
        addCity("нижний новгород", "Europe/Moscow", "нижний");
        addCity("ростов-на-дону", "Europe/Moscow", "ростов на дону", "ростов");
        addCity("казань", "Europe/Moscow");
        addCity("воронеж", "Europe/Moscow");
        addCity("краснодар", "Europe/Moscow");
        addCity("сочи", "Europe/Moscow");
        addCity("калининград", "Europe/Kaliningrad");
        addCity("мурманск", "Europe/Moscow");
        addCity("архангельск", "Europe/Moscow");
        addCity("ярославль", "Europe/Moscow");
        addCity("тверь", "Europe/Moscow");
        addCity("тула", "Europe/Moscow");
        addCity("рязань", "Europe/Moscow");
        addCity("саратов", "Europe/Saratov");
        addCity("волгоград", "Europe/Volgograd");
        addCity("астрахань", "Europe/Astrakhan");
        addCity("севастополь", "Europe/Moscow");
        addCity("симферополь", "Europe/Moscow");
        addCity("ялта", "Europe/Moscow");
        
        // Samara Time (UTC+4)
        addCity("самара", "Europe/Samara");
        addCity("ижевск", "Europe/Samara");
        addCity("ульяновск", "Europe/Samara");
        addCity("тольятти", "Europe/Samara");
        addCity("оренбург", "Asia/Yekaterinburg");
        
        // Yekaterinburg Time (UTC+5)
        addCity("екатеринбург", "Asia/Yekaterinburg", "екб");
        addCity("пермь", "Asia/Yekaterinburg");
        addCity("уфа", "Asia/Yekaterinburg");
        addCity("челябинск", "Asia/Yekaterinburg");
        addCity("тюмень", "Asia/Yekaterinburg");
        addCity("сургут", "Asia/Yekaterinburg");
        addCity("нижневартовск", "Asia/Yekaterinburg");
        addCity("курган", "Asia/Yekaterinburg");
        
        // Omsk Time (UTC+6)
        addCity("омск", "Asia/Omsk");
        addCity("новый уренгой", "Asia/Yekaterinburg");
        
        // Novosibirsk Time (UTC+7)
        addCity("новосибирск", "Asia/Novosibirsk", "нск");
        addCity("томск", "Asia/Tomsk");
        addCity("кемерово", "Asia/Novokuznetsk");
        addCity("барнаул", "Asia/Barnaul");
        addCity("новокузнецк", "Asia/Novokuznetsk");
        addCity("красноярск", "Asia/Krasnoyarsk");
        
        // Irkutsk Time (UTC+8)
        addCity("иркутск", "Asia/Irkutsk");
        addCity("улан-удэ", "Asia/Irkutsk", "улан удэ");
        addCity("чита", "Asia/Chita");
        addCity("братск", "Asia/Irkutsk");
        
        // Yakutsk Time (UTC+9)
        addCity("якутск", "Asia/Yakutsk");
        addCity("благовещенск", "Asia/Yakutsk");
        addCity("нерюнгри", "Asia/Yakutsk");
        
        // Vladivostok Time (UTC+10)
        addCity("владивосток", "Asia/Vladivostok");
        addCity("хабаровск", "Asia/Vladivostok");
        addCity("южно-сахалинск", "Asia/Sakhalin", "южно сахалинск");
        addCity("находка", "Asia/Vladivostok");
        addCity("уссурийск", "Asia/Vladivostok");
        
        // Magadan Time (UTC+11)
        addCity("магадан", "Asia/Magadan");
        
        // Kamchatka Time (UTC+12)
        addCity("петропавловск-камчатский", "Asia/Kamchatka", "петропавловск камчатский");
    }

    private static void addCity(String mainName, String timezone, String... aliases) {
        CITY_TIMEZONES.put(mainName, timezone);
        if (aliases.length > 0) {
            CITY_ALIASES.put(mainName, Arrays.asList(aliases));
            for (String alias : aliases) {
                CITY_TIMEZONES.put(alias, timezone);
            }
        }
    }

    public static ZoneId findTimeZoneByCity(String cityName) {
        if (!isValidCityInput(cityName)) {
            return null;
        }

        String normalizedCity = normalizeCity(cityName);
        String timezone = CITY_TIMEZONES.get(normalizedCity);
        
        if (timezone != null) {
            return ZoneId.of(timezone);
        }
        
        // Если точное совпадение не найдено, ищем по частичному совпадению
        for (Map.Entry<String, String> entry : CITY_TIMEZONES.entrySet()) {
            if (entry.getKey().contains(normalizedCity) || normalizedCity.contains(entry.getKey())) {
                return ZoneId.of(entry.getValue());
            }
        }
        
        return null; // Возвращаем null если город не найден
    }

    public static boolean isValidCity(String cityName) {
        if (!isValidCityInput(cityName)) {
            return false;
        }

        String normalizedCity = normalizeCity(cityName);
        
        // Проверяем точное совпадение
        if (CITY_TIMEZONES.containsKey(normalizedCity)) {
            return true;
        }
        
        // Проверяем частичное совпадение только если длина города достаточная
        if (normalizedCity.length() >= MIN_CITY_LENGTH) {
            for (String city : CITY_TIMEZONES.keySet()) {
                if (city.contains(normalizedCity) || normalizedCity.contains(city)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    private static boolean isValidCityInput(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            return false;
        }
        
        String normalized = normalizeCity(cityName);
        if (normalized.length() < MIN_CITY_LENGTH) {
            return false;
        }

        // Проверяем, что строка содержит хотя бы одну букву
        return normalized.matches(".*[а-яa-z].*");
    }

    private static String normalizeCity(String city) {
        return city.trim()
                  .toLowerCase()
                  .replace("ё", "е")
                  .replaceAll("[^а-яa-z\\s-]", "")
                  .replaceAll("\\s+", " ")
                  .trim();
    }

    public static String getSuggestedCities(String partialCity) {
        if (!isValidCityInput(partialCity)) {
            return "Пожалуйста, введите название города (минимум " + MIN_CITY_LENGTH + " буквы).";
        }

        StringBuilder suggestions = new StringBuilder("Возможно, вы имели в виду один из этих городов:\n");
        int count = 0;
        String normalized = normalizeCity(partialCity);
        
        for (String city : CITY_TIMEZONES.keySet()) {
            if (city.contains(normalized) || normalized.contains(city)) {
                suggestions.append("• ").append(city).append("\n");
                count++;
                if (count >= 5) break; // Показываем максимум 5 предложений
            }
        }
        
        return count > 0 ? suggestions.toString() : 
            "Извините, не могу найти указанный город. " +
                    "Пожалуйста, попробуйте еще раз или введите ближайший крупный город.";
    }
}
