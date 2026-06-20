package com.cinemabooking.backend.features.user.repository;

import com.cinemabooking.backend.features.user.dto.AttendanceDTO;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class AttendanceRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION = "attendance";

    public List<QueryDocumentSnapshot> findByStaffIdAndDate(String staffId, String date) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .whereEqualTo("staffId", staffId)
                .whereEqualTo("date", date)
                .get()
                .get()
                .getDocuments();
    }

    public void save(AttendanceDTO attendance) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(attendance.getId()).set(attendance).get();
    }

    public List<QueryDocumentSnapshot> findActiveCheckInsByDate(String date) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .whereEqualTo("date", date)
                .whereEqualTo("checkOutTime", 0L)
                .get()
                .get()
                .getDocuments();
    }

    public List<QueryDocumentSnapshot> findByStaffId(String staffId) throws ExecutionException, InterruptedException {
        return firestore.collection(COLLECTION)
                .whereEqualTo("staffId", staffId)
                .get()
                .get()
                .getDocuments();
    }

    public List<QueryDocumentSnapshot> findWithFilters(String staffId, String date, String cinemaId) throws ExecutionException, InterruptedException {
        Query query = firestore.collection(COLLECTION);
        if (staffId != null && !staffId.isEmpty()) {
            query = query.whereEqualTo("staffId", staffId);
        }
        if (date != null && !date.isEmpty()) {
            query = query.whereEqualTo("date", date);
        }
        if (cinemaId != null && !cinemaId.isEmpty()) {
            query = query.whereEqualTo("cinemaId", cinemaId);
        }
        return query.get().get().getDocuments();
    }
}
