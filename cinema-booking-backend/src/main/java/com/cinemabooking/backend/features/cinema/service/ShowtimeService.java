package com.cinemabooking.backend.features.cinema.service;
import com.cinemabooking.backend.features.voucher.service.VoucherService;

import com.cinemabooking.backend.features.cinema.dto.ShowtimeDTO;
import com.cinemabooking.backend.features.cinema.repository.CinemaRepository;
import com.cinemabooking.backend.features.cinema.repository.ShowtimeRepository;
import com.cinemabooking.backend.features.movie.repository.MovieRepository;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ShowtimeService {

    private static final Logger logger = LoggerFactory.getLogger(ShowtimeService.class);

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CinemaRepository cinemaRepository;
    
    @Autowired
    private VoucherService voucherService;

    public List<ShowtimeDTO> getAllShowtimes() throws ExecutionException, InterruptedException {
        logger.info("Fetching all showtimes from Firestore");
        List<ShowtimeDTO> showtimes = showtimeRepository.findAllOrderedByStartAt().stream()
                .filter(dto -> !Boolean.TRUE.equals(dto.isDeleted()))
                .collect(Collectors.toList());
        logger.info("Loaded {} showtimes", showtimes.size());
        return showtimes;
    }

    public List<ShowtimeDTO> getShowtimesBetween(long start, long end) throws ExecutionException, InterruptedException {
        logger.info("Fetching showtimes between {} and {}", start, end);
        return showtimeRepository.findShowtimesBetween(start, end).stream()
                .filter(dto -> !Boolean.TRUE.equals(dto.isDeleted()))
                .collect(Collectors.toList());
    }

    public ShowtimeDTO getShowtimeById(String id) throws ExecutionException, InterruptedException {
        logger.info("Fetching showtime by ID: {}", id);
        DocumentSnapshot doc = showtimeRepository.findById(id);
        if (doc.exists()) {
            ShowtimeDTO dto = doc.toObject(ShowtimeDTO.class);
            if (dto != null && !Boolean.TRUE.equals(dto.isDeleted())) {
                return dto;
            }
        }
        return null;
    }

    public List<ShowtimeDTO> getShowtimesByMovieId(String movieId) throws ExecutionException, InterruptedException {
        logger.info("Fetching showtimes for movieId: {}", movieId);
        List<QueryDocumentSnapshot> documents = showtimeRepository.findByMovieId(movieId);
        List<ShowtimeDTO> showtimes = processDocuments(documents);
        showtimes.sort(java.util.Comparator.comparingLong(ShowtimeDTO::getStartAt));
        logger.info("Loaded {} showtimes for movie {}", showtimes.size(), movieId);
        return showtimes;
    }

    public List<ShowtimeDTO> getShowtimesByCinemaId(String cinemaId) throws ExecutionException, InterruptedException {
        logger.info("Fetching showtimes for cinemaId: {}", cinemaId);
        List<QueryDocumentSnapshot> documents = showtimeRepository.findByCinemaId(cinemaId);
        List<ShowtimeDTO> showtimes = processDocuments(documents);
        showtimes.sort(java.util.Comparator.comparingLong(ShowtimeDTO::getStartAt));
        logger.info("Loaded {} showtimes for cinema {}", showtimes.size(), cinemaId);
        return showtimes;
    }

    private List<ShowtimeDTO> processDocuments(List<QueryDocumentSnapshot> documents) {
        return documents.stream()
                .map(this::mapToDTO)
                .filter(dto -> dto != null && !dto.isDeleted())
                .collect(Collectors.toList());
    }

    private ShowtimeDTO mapToDTO(DocumentSnapshot doc) {
        try {
            return ShowtimeDTO.builder()
                    .showtimeId(doc.getId())
                    .movieId(doc.getString("movieId"))
                    .cinemaId(doc.getString("cinemaId"))
                    .roomId(doc.getString("roomId"))
                    .startAt(doc.getLong("startAt") != null ? doc.getLong("startAt") : 0L)
                    .endAt(doc.getLong("endAt") != null ? doc.getLong("endAt") : 0L)
                    .basePrice(doc.getDouble("basePrice") != null ? doc.getDouble("basePrice") : 0.0)
                    .format(doc.getString("format"))
                    .language(doc.getString("language"))
                    .status(doc.getString("status"))
                    .totalSeats(doc.getLong("totalSeats") != null ? doc.getLong("totalSeats").intValue() : 0)
                    .bookedSeatsCount(doc.getLong("bookedSeatsCount") != null ? doc.getLong("bookedSeatsCount").intValue() : 0)
                    .createdAt(doc.getLong("createdAt") != null ? doc.getLong("createdAt") : 0L)
                    .updatedAt(doc.getLong("updatedAt") != null ? doc.getLong("updatedAt") : 0L)
                    .deleted(Boolean.TRUE.equals(doc.getBoolean("deleted")))
                    .build();
        } catch (Exception e) {
            logger.warn("Error mapping showtime doc {}: {}", doc.getId(), e.getMessage());
            return null;
        }
    }

    public int seedShowtimes() throws ExecutionException, InterruptedException {
        logger.info("Starting showtime seeding...");
        
        // 1. Fetch active movies
        List<QueryDocumentSnapshot> movieDocs = movieRepository.findAll();
        List<java.util.Map<String, Object>> movies = new ArrayList<>();
        for (DocumentSnapshot doc : movieDocs) {
            Boolean deleted = doc.getBoolean("deleted");
            Boolean isActive = doc.getBoolean("isActive");
            if (Boolean.TRUE.equals(deleted) || Boolean.FALSE.equals(isActive)) continue;
            java.util.Map<String, Object> m = doc.getData();
            if (m != null) {
                m.put("movieId", doc.getId());
                movies.add(m);
            }
        }
        logger.info("Found {} active movies for seeding", movies.size());
        
        // 2. Fetch active cinemas
        List<QueryDocumentSnapshot> cinemaDocs = cinemaRepository.findAllCinemas();
        List<java.util.Map<String, Object>> cinemas = new ArrayList<>();
        for (DocumentSnapshot doc : cinemaDocs) {
            Boolean deleted = doc.getBoolean("deleted");
            if (Boolean.TRUE.equals(deleted)) continue;
            java.util.Map<String, Object> c = doc.getData();
            if (c != null) {
                c.put("cinemaId", doc.getId());
                cinemas.add(c);
            }
        }
        logger.info("Found {} active cinemas for seeding", cinemas.size());
        
        if (movies.isEmpty() || cinemas.isEmpty()) {
            logger.warn("Seeding aborted: movies or cinemas are empty!");
            return 0;
        }

        // 3. Fetch active rooms
        List<QueryDocumentSnapshot> roomDocs = cinemaRepository.findAllRooms();
        List<java.util.Map<String, Object>> rooms = new ArrayList<>();
        for (DocumentSnapshot doc : roomDocs) {
            Boolean deleted = doc.getBoolean("deleted");
            if (Boolean.TRUE.equals(deleted)) continue;
            java.util.Map<String, Object> r = doc.getData();
            if (r != null) {
                r.put("roomId", doc.getId());
                rooms.add(r);
            }
        }
        logger.info("Found {} active rooms for seeding", rooms.size());

        long now = System.currentTimeMillis();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        long tomorrowMidnight = cal.getTimeInMillis();
        long dayMs = 24 * 60 * 60 * 1000L;

        int cinemaIndex = 0;
        int seedCount = 0;

        for (java.util.Map<String, Object> movie : movies) {
            String movieId = (String) movie.get("movieId");
            String movieTitle = (String) movie.get("title");
            
            // Pick a cinema round-robin
            java.util.Map<String, Object> cinema = cinemas.get(cinemaIndex % cinemas.size());
            cinemaIndex++;
            String cinemaId = (String) cinema.get("cinemaId");

            // Filter rooms for this cinema
            List<java.util.Map<String, Object>> cinemaRooms = new ArrayList<>();
            for (java.util.Map<String, Object> r : rooms) {
                if (cinemaId.equals(r.get("cinemaId"))) {
                    cinemaRooms.add(r);
                }
            }

            if (cinemaRooms.isEmpty()) {
                logger.warn("Cinema {} has no active rooms, skipping movie {}", cinemaId, movieTitle);
                continue;
            }

            // Prepare 6 showtimes configs
            int[][] schedule = {
                {0, 0, 9, 0},   // Day 1, Room index 0, 09:00
                {0, 1 % cinemaRooms.size(), 12, 0},  // Day 1, Room index 1, 12:00
                {0, 2 % cinemaRooms.size(), 15, 0},  // Day 1, Room index 2, 15:00
                {1, 0, 18, 0},  // Day 2, Room index 0, 18:00
                {1, 1 % cinemaRooms.size(), 21, 0},  // Day 2, Room index 1, 21:00
                {1, 2 % cinemaRooms.size(), 13, 30}  // Day 2, Room index 2, 13:30
            };

            Number durationNum = (Number) movie.get("durationMinutes");
            long durationMs = (durationNum != null ? durationNum.longValue() : 120L) * 60 * 1000L;

            for (int i = 0; i < 6; i++) {
                int[] slot = schedule[i];
                int dayOffset = slot[0];
                int roomIdx = slot[1];
                int hour = slot[2];
                int minute = slot[3];

                java.util.Map<String, Object> room = cinemaRooms.get(roomIdx);
                String roomId = (String) room.get("roomId");
                Number totalSeatsNum = (Number) room.get("totalSeats");
                int totalSeats = totalSeatsNum != null ? totalSeatsNum.intValue() : 64;

                long startAt = tomorrowMidnight + (dayOffset * dayMs) + (hour * 60 * 60 * 1000L) + (minute * 60 * 1000L);
                long endAt = startAt + durationMs;

                String mPrefix = movieId.substring(0, Math.min(movieId.length(), 6));
                String rPrefix = roomId.substring(0, Math.min(roomId.length(), 6));
                String showtimeId = "ST-SEED-" + mPrefix + "-" + rPrefix + "-" + (startAt / 60000);

                DocumentSnapshot existingDoc = showtimeRepository.findById(showtimeId);
                if (!existingDoc.exists()) {
                    java.util.Map<String, Object> showtimeData = new java.util.HashMap<>();
                    showtimeData.put("showtimeId", showtimeId);
                    showtimeData.put("movieId", movieId);
                    showtimeData.put("cinemaId", cinemaId);
                    showtimeData.put("roomId", roomId);
                    showtimeData.put("startAt", startAt);
                    showtimeData.put("endAt", endAt);
                    showtimeData.put("basePrice", 85000.0);
                    showtimeData.put("format", "2D PHỤ ĐỀ");
                    showtimeData.put("language", "Vietnamese Sub");
                    showtimeData.put("status", "active");
                    showtimeData.put("totalSeats", totalSeats);
                    showtimeData.put("bookedSeatsCount", 0);
                    showtimeData.put("deleted", false);
                    showtimeData.put("createdAt", now);
                    showtimeData.put("updatedAt", now);

                    showtimeRepository.saveMap(showtimeId, showtimeData);
                    seedCount++;
                } else if ("AVAILABLE".equals(existingDoc.getString("status"))) {
                    java.util.Map<String, Object> updateData = new java.util.HashMap<>();
                    updateData.put("status", "active");
                    showtimeRepository.updateMap(showtimeId, updateData);
                    seedCount++;
                }
            }
        }
        logger.info("Successfully seeded {} showtimes", seedCount);
        return seedCount;
    }

    public void cancelShowtime(String showtimeId) throws ExecutionException, InterruptedException {
        logger.info("Cancelling showtime: {}", showtimeId);
        showtimeRepository.updateStatus(showtimeId, "CANCELLED", System.currentTimeMillis());
        // Generate compensation vouchers automatically
        voucherService.generateVouchersForCancelledShowtime(showtimeId, 100, 30);
    }

    public ShowtimeDTO updateShowtime(ShowtimeDTO showtime) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = showtimeRepository.getDocumentReference(showtime.getShowtimeId());
        return showtimeRepository.runTransaction(transaction -> {
            ShowtimeDTO t_showtime = transaction.get(documentReference).get().toObject(ShowtimeDTO.class);
            if (t_showtime == null) {
                throw new RuntimeException("Showtime not found with ID: " + t_showtime.getShowtimeId());
            }

            t_showtime.setBasePrice(showtime.getBasePrice());
            t_showtime.setLanguage(showtime.getLanguage());
            t_showtime.setFormat(showtime.getFormat());
            t_showtime.setUpdatedAt(System.currentTimeMillis());

            if(t_showtime.getBookedSeatsCount() <= 0){
                t_showtime.setStartAt(showtime.getStartAt());
                t_showtime.setEndAt(showtime.getEndAt());
                t_showtime.setRoomId(showtime.getRoomId());
                t_showtime.setCinemaId(showtime.getCinemaId());

                transaction.set(documentReference, t_showtime, SetOptions.mergeFields("startAt", "endAt", "roomId", "cinemaId", "basePrice", "language", "format", "updatedAt"));;
            }else{
                transaction.set(documentReference, t_showtime, SetOptions.mergeFields("basePrice", "language", "format", "updatedAt"));
            }

            return t_showtime;
        });
    }
}

