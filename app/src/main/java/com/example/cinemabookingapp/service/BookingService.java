package com.example.cinemabookingapp.service;

import com.example.cinemabookingapp.core.constants.FirestoreCollections;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Booking;
import com.example.cinemabookingapp.domain.model.Movie;
import com.example.cinemabookingapp.domain.model.Showtime;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.stream.Collectors;

public class BookingService {
    public class BookingDetail{
        public Booking booking;
        public Showtime showtime;
        public Movie movie;
    }

    FirebaseFirestore firestore;
    public BookingService(){
        firestore = FirebaseFirestore.getInstance();
    }

    public void loadUserBookingHistory(String userId, ResultCallback<List<BookingDetail>> callback){
        firestore.collection(FirestoreCollections.BOOKINGS)
                .whereEqualTo("userId", userId)
                .get()
                .continueWithTask(task -> {
                    List<Booking> bookings = task.getResult().toObjects(Booking.class);

                    return Tasks.whenAllSuccess(bookings.stream().map(booking ->{
                                BookingDetail invoice = new BookingDetail();
                                invoice.booking = booking;
                                return firestore.collection(FirestoreCollections.SHOWTIMES)
                                        .document(booking.showtimeId)
                                        .get()
                                        .continueWithTask(task1 -> {
                                            Showtime showtime = task1.getResult().toObject(Showtime.class);
                                            invoice.showtime = showtime;
                                            return firestore.collection(FirestoreCollections.MOVIES)
                                                    .document(showtime.movieId)
                                                    .get();
                                        })
                                        .continueWith(task1 -> {
                                            Movie movie = task1.getResult().toObject(Movie.class);
                                            invoice.movie = movie;
                                            return invoice;
                                        });
                            }
                    ).collect(Collectors.toList()));
                })
                .addOnSuccessListener(objects -> {
                    callback.onSuccess(objects.stream().map(obj -> (BookingDetail) obj).collect(Collectors.toList()));
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                    e.printStackTrace();
                });

    }
}
