package com.example.cinemabookingapp.ui.customer.model;

import com.example.cinemabookingapp.domain.model.Cinema;
import com.example.cinemabookingapp.domain.model.Showtime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MovieDetailScheduleCatalog {

    private final LinkedHashMap<String, List<CinemaSection>> cinemasByCity = new LinkedHashMap<>();
    private final List<DateOption> dateOptions = new ArrayList<>();

    public static MovieDetailScheduleCatalog createDefault() {
        MovieDetailScheduleCatalog catalog = new MovieDetailScheduleCatalog();
        catalog.buildDefaultDates();
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

    public void clearData() {
        cinemasByCity.clear();
    }

    public void buildFromData(List<Showtime> showtimes, List<Cinema> cinemas) {

        clearData();

        if (showtimes == null || cinemas == null) {
            android.util.Log.e(
                    "ScheduleCatalog",
                    "ABORT: showtimes or cinemas is null"
            );
            return;
        }

        android.util.Log.d(
                "ScheduleCatalog",
                ">>> [CATALOG-1] START buildFromData: showtimes="
                        + showtimes.size()
                        + " cinemas="
                        + cinemas.size()
        );

        /*
         * =========================
         * BUILD CINEMA MAP
         * =========================
         */

        Map<String, Cinema> cinemaMap = new HashMap<>();

        for (Cinema cinema : cinemas) {

            if (cinema == null) {
                continue;
            }

            if (cinema.cinemaId == null) {
                continue;
            }

            String cinemaId = cinema.cinemaId.trim();

            if (cinemaId.isEmpty()) {
                continue;
            }

            cinemaMap.put(cinemaId, cinema);

            android.util.Log.d(
                    "ScheduleCatalog",
                    "CinemaMap ADD -> "
                            + cinemaId
                            + " | "
                            + cinema.name
            );
        }

        SimpleDateFormat timeFormatter =
                new SimpleDateFormat("HH:mm", Locale.getDefault());

        SimpleDateFormat dateFormatter =
                new SimpleDateFormat("dd/MM", Locale.getDefault());

        int mappedCount = 0;

        /*
         * =========================
         * MAP SHOWTIMES
         * =========================
         */

        for (Showtime st : showtimes) {

            try {

                if (st == null) {
                    continue;
                }

                if (st.movieId == null) {
                    android.util.Log.e(
                            "ScheduleCatalog",
                            "SKIP: movieId null"
                    );
                    continue;
                }

                if (st.cinemaId == null) {
                    android.util.Log.e(
                            "ScheduleCatalog",
                            "SKIP: cinemaId null"
                    );
                    continue;
                }

                String cinemaId = st.cinemaId.trim();

                Cinema cinema = cinemaMap.get(cinemaId);

                if (cinema == null) {
                    android.util.Log.e(
                            "ScheduleCatalog",
                            ">>> [CATALOG-SKIP] MISMATCH cinemaId = " + cinemaId + " for showtime " + st.showtimeId
                    );
                    continue;
                }

                mappedCount++;

                /*
                 * =========================
                 * CITY
                 * =========================
                 */

                String city = "Khác";

                if (cinema.city != null
                        && !cinema.city.trim().isEmpty()) {

                    city = cinema.city.trim();
                }

                addCity(city);

                List<CinemaSection> sections =
                        cinemasByCity.get(city);

                if (sections == null) {
                    sections = new ArrayList<>();
                    cinemasByCity.put(city, sections);
                }

                /*
                 * =========================
                 * FIND / CREATE CINEMA SECTION
                 * =========================
                 */

                CinemaSection targetSection = null;

                for (CinemaSection section : sections) {

                    if (section.cinemaId.equals(cinemaId)) {
                        targetSection = section;
                        break;
                    }
                }

                if (targetSection == null) {

                    targetSection = new CinemaSection(
                            cinemaId,
                            cinema.name,
                            false,
                            new ArrayList<>()
                    );

                    sections.add(targetSection);

                    android.util.Log.d(
                            "ScheduleCatalog",
                            "ADD CINEMA SECTION -> "
                                    + cinema.name
                    );
                }

                /*
                 * =========================
                 * GROUP FORMAT
                 * =========================
                 */

                String format = "2D";

                if (st.format != null
                        && !st.format.trim().isEmpty()) {

                    format = st.format.trim();
                }

                ShowtimeGroup targetGroup = null;

                for (ShowtimeGroup group : targetSection.groups) {

                    if (group.title.equals(format)) {
                        targetGroup = group;
                        break;
                    }
                }

                if (targetGroup == null) {

                    targetGroup = new ShowtimeGroup(
                            format,
                            new ArrayList<>()
                    );

                    targetSection.groups.add(targetGroup);

                    android.util.Log.d(
                            "ScheduleCatalog",
                            "ADD FORMAT GROUP -> " + format
                    );
                }

                /*
                 * =========================
                 * CREATE SHOWTIME INFO
                 * =========================
                 */

                long timestamp = st.startAt;

                String timeText =
                        timeFormatter.format(timestamp);

                String dateText =
                        dateFormatter.format(timestamp);

                ShowtimeInfo info = new ShowtimeInfo(
                        st.showtimeId,
                        timeText,
                        dateText,
                        st.roomId,
                        format,
                        st.basePrice,
                        timestamp
                );

                targetGroup.addShowtime(info);

                android.util.Log.d(
                        "ScheduleCatalog",
                        ">>> [CATALOG-ADD] ID="
                                + st.showtimeId
                                + " | Cinema="
                                + cinema.name
                                + " | Time="
                                + timeText
                                + " | Date="
                                + dateText
                                + " | Format="
                                + format
                );

            } catch (Exception e) {

                android.util.Log.e(
                        "ScheduleCatalog",
                        "ERROR parsing showtime: "
                                + e.getMessage()
                );
            }
        }

        android.util.Log.d(
                "ScheduleCatalog",
                "FINISH buildFromData -> mapped="
                        + mappedCount
                        + " cities="
                        + cinemasByCity.size()
        );

        /*
         * DEBUG RESULT
         */

        for (String city : cinemasByCity.keySet()) {

            List<CinemaSection> sections =
                    cinemasByCity.get(city);

            android.util.Log.d(
                    "ScheduleCatalog",
                    "CITY -> "
                            + city
                            + " | cinemas="
                            + (sections != null ? sections.size() : 0)
            );

            if (sections != null) {

                for (CinemaSection section : sections) {

                    android.util.Log.d(
                            "ScheduleCatalog",
                            "   CINEMA -> "
                                    + section.name
                                    + " groups="
                                    + section.groups.size()
                    );

                    for (ShowtimeGroup group : section.groups) {

                        android.util.Log.d(
                                "ScheduleCatalog",
                                "      GROUP -> "
                                        + group.title
                                        + " showtimes="
                                        + group.showtimes.size()
                        );
                    }
                }
            }
        }
    }

    public void addCity(String city) {

        if (city == null) {
            return;
        }

        city = city.trim();

        if (city.isEmpty()) {
            return;
        }

        if (!cinemasByCity.containsKey(city)) {

            cinemasByCity.put(
                    city,
                    new ArrayList<>()
            );
        }
    }

    public void setExpandedCinema(
            String city,
            String cinemaName
    ) {

        List<CinemaSection> sections =
                cinemasByCity.get(city);

        if (sections == null) {
            return;
        }

        for (CinemaSection section : sections) {

            section.expanded =
                    section.name.equals(cinemaName);
        }
    }

    private void buildDefaultDates() {

        dateOptions.clear();

        Calendar today = Calendar.getInstance();

        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "dd/MM",
                        Locale.getDefault()
                );

        for (int i = 0; i < 7; i++) {

            Calendar day =
                    (Calendar) today.clone();

            day.add(Calendar.DAY_OF_MONTH, i);

            String label =
                    i == 0
                            ? "Hôm nay"
                            : dayLabel(day.get(Calendar.DAY_OF_WEEK));

            String dateText =
                    formatter.format(day.getTime());

            dateOptions.add(
                    new DateOption(
                            label,
                            dateText
                    )
            );
            
            android.util.Log.d("ScheduleCatalog", ">>> [CATALOG-DATE] Chip " + i + ": " + label + " (" + dateText + ")");
        }
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

    /*
     * =========================
     * MODELS
     * =========================
     */

    public static class DateOption {

        public final String label;
        public final String dateText;

        public DateOption(
                String label,
                String dateText
        ) {
            this.label = label;
            this.dateText = dateText;
        }
    }

    public static class CinemaSection {

        public final String cinemaId;
        public final String name;
        public boolean expanded;
        public final List<ShowtimeGroup> groups;

        public CinemaSection(
                String cinemaId,
                String name,
                boolean expanded,
                List<ShowtimeGroup> groups
        ) {
            this.cinemaId = cinemaId;
            this.name = name;
            this.expanded = expanded;
            this.groups = groups;
        }
    }

    public static class ShowtimeGroup {

        public final String title;
        public final List<ShowtimeInfo> showtimes;

        public ShowtimeGroup(
                String title,
                List<ShowtimeInfo> showtimes
        ) {
            this.title = title;
            this.showtimes = showtimes;
        }

        public void addShowtime(ShowtimeInfo info) {

            this.showtimes.add(info);

            Collections.sort(
                    showtimes,
                    (a, b) -> Long.compare(
                            a.timestamp,
                            b.timestamp
                    )
            );
        }
    }

    public static class ShowtimeInfo {

        public final String showtimeId;
        public final String time;
        public final String dateText;
        public final String roomId;
        public final String format;
        public final double price;
        public final long timestamp;

        public ShowtimeInfo(
                String showtimeId,
                String time,
                String dateText,
                String roomId,
                String format,
                double price,
                long timestamp
        ) {
            this.showtimeId = showtimeId;
            this.time = time;
            this.dateText = dateText;
            this.roomId = roomId;
            this.format = format;
            this.price = price;
            this.timestamp = timestamp;
        }
    }

    public List<String> getCinemaNames(String city) {

        List<CinemaSection> sections =
                cinemasByCity.get(city);

        List<String> names =
                new ArrayList<>();

        if (sections != null) {

            for (CinemaSection section : sections) {
                names.add(section.name);
            }
        }

        return names;
    }
}