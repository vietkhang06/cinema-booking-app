package com.example.cinemabookingapp.ui.customer.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class MovieDetailScheduleCatalog {

    private final LinkedHashMap<String, List<CinemaSection>> cinemasByCity = new LinkedHashMap<>();
    private final List<DateOption> dateOptions = new ArrayList<>();

    public static MovieDetailScheduleCatalog createDefault() {
        MovieDetailScheduleCatalog catalog = new MovieDetailScheduleCatalog();
        catalog.buildDefaultDates();
        catalog.seedSampleData();
        return catalog;
    }

    public List<String> getCityNames() {
        return new ArrayList<>(cinemasByCity.keySet());
    }

    public List<CinemaSection> getCinemas(String city) {
        List<CinemaSection> sections = cinemasByCity.get(city);
        if (sections == null) {
            return new ArrayList<>();
        }
        return sections;
    }

    public List<DateOption> getDateOptions() {
        return dateOptions;
    }

    public void addCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            return;
        }
        if (!cinemasByCity.containsKey(city)) {
            cinemasByCity.put(city, new ArrayList<>());
        }
    }

    public void addCinema(String city, CinemaSection section) {
        if (city == null || city.trim().isEmpty() || section == null) {
            return;
        }
        addCity(city);
        cinemasByCity.get(city).add(section);
    }

    public void setExpandedCinema(String city, String cinemaName) {
        List<CinemaSection> sections = cinemasByCity.get(city);
        if (sections == null) {
            return;
        }
        for (CinemaSection section : sections) {
            section.expanded = section.name.equals(cinemaName);
        }
    }

    public void collapseAll(String city) {
        List<CinemaSection> sections = cinemasByCity.get(city);
        if (sections == null) {
            return;
        }
        for (CinemaSection section : sections) {
            section.expanded = false;
        }
    }

    private void buildDefaultDates() {
        dateOptions.clear();

        Calendar today = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) today.clone();
            day.add(Calendar.DAY_OF_MONTH, i);

            String label = i == 0 ? "Hôm nay" : dayLabel(day.get(Calendar.DAY_OF_WEEK));
            String dateText = format.format(day.getTime());

            dateOptions.add(new DateOption(label, dateText));
        }
    }

    private void seedSampleData() {
        addCinema("Hà Nội", new CinemaSection(
                "Galaxy CineX",
                true,
                Arrays.asList(
                        new ShowtimeGroup("2D PHỤ ĐỀ", Arrays.asList("10:00", "12:15", "14:15", "16:15", "18:15", "20:15", "22:15")),
                        new ShowtimeGroup("VIP - LAGOM 2D PHỤ ĐỀ", Arrays.asList("15:15", "19:15", "21:15"))
                )
        ));

        addCinema("Hà Nội", new CinemaSection(
                "Galaxy Nguyễn Du",
                false,
                Arrays.asList(
                        new ShowtimeGroup("2D PHỤ ĐỀ", Arrays.asList("10:30", "13:00", "15:30", "18:30", "21:30"))
                )
        ));

        addCinema("Hà Nội", new CinemaSection(
                "Galaxy Sala",
                false,
                Arrays.asList(
                        new ShowtimeGroup("2D PHỤ ĐỀ", Arrays.asList("11:00", "14:00", "17:00", "20:00"))
                )
        ));

        addCinema("TP. Hồ Chí Minh", new CinemaSection(
                "CGV Vincom",
                true,
                Arrays.asList(
                        new ShowtimeGroup("2D PHỤ ĐỀ", Arrays.asList("09:45", "12:00", "14:30", "17:00", "19:30", "22:00")),
                        new ShowtimeGroup("VIP", Arrays.asList("15:45", "18:45", "21:45"))
                )
        ));

        addCinema("TP. Hồ Chí Minh", new CinemaSection(
                "Galaxy Sala",
                false,
                Arrays.asList(
                        new ShowtimeGroup("2D PHỤ ĐỀ", Arrays.asList("10:15", "13:15", "16:15", "19:15", "22:15"))
                )
        ));

        addCinema("TP. Hồ Chí Minh", new CinemaSection(
                "Lotte Cinema",
                false,
                Arrays.asList(
                        new ShowtimeGroup("2D PHỤ ĐỀ", Arrays.asList("11:30", "14:30", "17:30", "20:30"))
                )
        ));

        addCinema("Đà Nẵng", new CinemaSection(
                "Galaxy Đà Nẵng",
                true,
                Arrays.asList(
                        new ShowtimeGroup("2D PHỤ ĐỀ", Arrays.asList("10:00", "12:30", "15:00", "17:30", "20:00"))
                )
        ));

        addCinema("Đà Nẵng", new CinemaSection(
                "CGV Vincom Đà Nẵng",
                false,
                Arrays.asList(
                        new ShowtimeGroup("2D PHỤ ĐỀ", Arrays.asList("11:00", "14:00", "17:00", "20:30"))
                )
        ));

        addCinema("Hải Phòng", new CinemaSection(
                "Lotte Hải Phòng",
                true,
                Arrays.asList(
                        new ShowtimeGroup("2D PHỤ ĐỀ", Arrays.asList("09:30", "12:00", "14:30", "17:00", "19:30"))
                )
        ));

        addCinema("Hải Phòng", new CinemaSection(
                "Galaxy Hải Phòng",
                false,
                Arrays.asList(
                        new ShowtimeGroup("2D PHỤ ĐỀ", Arrays.asList("10:45", "13:45", "16:45", "19:45"))
                )
        ));
    }

    private String dayLabel(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                return "Thứ 2";
            case Calendar.TUESDAY:
                return "Thứ 3";
            case Calendar.WEDNESDAY:
                return "Thứ 4";
            case Calendar.THURSDAY:
                return "Thứ 5";
            case Calendar.FRIDAY:
                return "Thứ 6";
            case Calendar.SATURDAY:
                return "Thứ 7";
            case Calendar.SUNDAY:
            default:
                return "CN";
        }
    }

    public static class DateOption {
        public final String label;
        public final String dateText;

        public DateOption(String label, String dateText) {
            this.label = label;
            this.dateText = dateText;
        }
    }

    public static class CinemaSection {
        public final String name;
        public boolean expanded;
        public final List<ShowtimeGroup> groups;

        public CinemaSection(String name, boolean expanded, List<ShowtimeGroup> groups) {
            this.name = name;
            this.expanded = expanded;
            this.groups = groups;
        }
    }

    public static class ShowtimeGroup {
        public final String title;
        public final List<String> times;

        public ShowtimeGroup(String title, List<String> times) {
            this.title = title;
            this.times = times;
        }
    }

    public List<String> getCinemaNames(String city) {
        List<CinemaSection> sections = cinemasByCity.get(city);
        List<String> names = new ArrayList<>();

        if (sections != null) {
            for (CinemaSection section : sections) {
                names.add(section.name);
            }
        }

        return names;
    }
}