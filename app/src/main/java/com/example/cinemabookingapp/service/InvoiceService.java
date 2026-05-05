package com.example.cinemabookingapp.service;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.cinemabookingapp.core.constants.FirestoreCollections;
import com.example.cinemabookingapp.domain.model.Booking;
import com.example.cinemabookingapp.domain.model.Movie;
import com.example.cinemabookingapp.domain.model.Showtime;
import com.example.cinemabookingapp.domain.model.Snack;
import com.example.cinemabookingapp.domain.model.SnackOrder;
import com.example.cinemabookingapp.domain.model.SnackOrderItem;
import com.example.cinemabookingapp.domain.repository.MovieRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.Record;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceService {
    public class InvoiceDetail{
        public Booking booking;
        public Showtime showtime;
        public SnackOrder snackOrder;

        // snack name,price,quantity
        public List<String> snackItems;
        public Movie movie;


    }
    public static final String logTag = "Invoice Service";
    FirebaseFirestore firestore;
    MovieRepository movieRepository;
    public InvoiceService(){
        firestore = FirebaseFirestore.getInstance();
    }
    public void updatePaymentStatus(String invoiceId, String status){
        
    }

    public Booking getInvoiceFromId(String invoiceId){
        try {
            Task<DocumentSnapshot> getInvoiceTask = firestore.collection(FirestoreCollections.BOOKINGS)
                    .document(invoiceId).get();
            return Tasks.await(getInvoiceTask).toObject(Booking.class);
        }catch (Exception e){
            Log.e(logTag, e.getMessage());
            return null;
        }
    }

    @Nullable
    public InvoiceDetail getInvoiceDetail(String invoiceId){
        try {
            InvoiceDetail invoiceDetail = new InvoiceDetail();

            invoiceDetail.booking = Tasks.await(firestore.collection(FirestoreCollections.BOOKINGS).document(invoiceId).get()).toObject(Booking.class);

            Tasks.await(Tasks.whenAll(
                firestore.collection(FirestoreCollections.SHOWTIMES).document(invoiceDetail.booking.showtimeId).get()
                        .addOnSuccessListener(documentSnapshot ->
                                invoiceDetail.showtime = documentSnapshot.toObject(Showtime.class)
                        ),
                firestore.collection(FirestoreCollections.SNACK_ORDERS).document(invoiceDetail.booking.snackOrderId).get()
                        .addOnSuccessListener(documentSnapshot ->
                                invoiceDetail.snackOrder = documentSnapshot.toObject(SnackOrder.class)
                        )
            ));

            Tasks.await(Tasks.whenAllComplete(
                invoiceDetail.snackOrder.items.stream().filter(snackOrderItem -> snackOrderItem.snackId != null && !snackOrderItem.snackId.isBlank())
                        .map(snackOrderItem ->
                            firestore.collection(FirestoreCollections.SNACKS).document(snackOrderItem.snackId).get())
                        .collect(Collectors.toList())
            ).addOnSuccessListener((tasks) -> {
                invoiceDetail.snackItems = tasks.stream()
                        .filter(task -> task.isSuccessful())
                        .map(task -> {
                            DocumentSnapshot documentSnapshot = (DocumentSnapshot) task.getResult();
                            Snack snack = documentSnapshot.toObject(Snack.class);
                            return String.format("%s,%s,", snack.name, snack.price);
                        })
                        .collect(Collectors.toList());

                for(int i = 0; i < invoiceDetail.snackOrder.items.size(); i++){
                    invoiceDetail.snackItems.set(i, invoiceDetail.snackItems.get(i) + invoiceDetail.snackOrder.items.get(i).quantity);
                }
            }));

            invoiceDetail.movie = Tasks.await(firestore.collection(FirestoreCollections.MOVIES).document(invoiceDetail.showtime.movieId).get()).toObject(Movie.class);


            return invoiceDetail;
        }catch (Exception e){
            Log.e(logTag, e.getMessage());
            return null;
        }
    }

}
