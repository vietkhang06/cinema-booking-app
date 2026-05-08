package com.example.cinemabookingapp.service;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.cinemabookingapp.core.constants.FirestoreCollections;
import com.example.cinemabookingapp.domain.common.ResultCallback;
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
import com.google.firebase.firestore.SetOptions;

import java.lang.Record;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public void updatePaymentStatus(Booking booking, ResultCallback<Void> resultCallback){

        firestore.collection(FirestoreCollections.BOOKINGS)
                .document(booking.bookingId)
                .set(booking, SetOptions.mergeFields("paymentStatus", "paymentMethod", ""))
                .addOnSuccessListener(unused -> {
                    resultCallback.onSuccess(unused);
                })
                .addOnFailureListener(e -> {
                    resultCallback.onError(e.getMessage());
                });
    }

    public void getInvoiceFromId(String invoiceId, ResultCallback<Booking> callback){
        firestore.collection(FirestoreCollections.BOOKINGS)
            .document(invoiceId).get()
            .addOnSuccessListener(documentSnapshot -> {
                callback.onSuccess(documentSnapshot.toObject(Booking.class));
            })
            .addOnFailureListener(e -> {
                callback.onError(e.getMessage());
            });
    }

    public void getInvoiceDetail(String invoiceId, ResultCallback<InvoiceDetail> callback){

            InvoiceDetail invoiceDetail = new InvoiceDetail();

            firestore.collection(FirestoreCollections.BOOKINGS).document(invoiceId).get()
                    .continueWithTask(task -> {
                        invoiceDetail.booking = task.getResult().toObject(Booking.class);
                        return firestore.collection(FirestoreCollections.SHOWTIMES).document(invoiceDetail.booking.showtimeId).get();

                    })
                    .continueWithTask(task -> {
                        invoiceDetail.showtime = task.getResult().toObject(Showtime.class);
                        return firestore.collection(FirestoreCollections.MOVIES)
                                .document(invoiceDetail.showtime.movieId)
                                .get();
                    })
                    .addOnSuccessListener(documentSnapshot -> {
                        invoiceDetail.movie = documentSnapshot.toObject(Movie.class);
                        if(invoiceDetail.booking.snackOrderId != null && !invoiceDetail.booking.snackOrderId.isBlank()){
                            firestore.collection(FirestoreCollections.SNACK_ORDERS)
                                    .document(invoiceDetail.booking.snackOrderId)
                                    .get()
                                .continueWithTask(task -> {
                                    invoiceDetail.snackOrder = task.getResult().toObject(SnackOrder.class);
                                    if(invoiceDetail.snackOrder == null)
                                        throw new Exception("Nack Order not found");
                                    return firestore.collection(FirestoreCollections.SNACKS)
                                            .whereIn("snackId", invoiceDetail.snackOrder.items.stream().map(item -> item.snackId).collect(Collectors.toList()))
                                            .get();
                                })
                                .addOnSuccessListener(documentSnapshots -> {
                                    List<Snack> snacks = documentSnapshots.toObjects(Snack.class);

                                    invoiceDetail.snackItems = new ArrayList<>();
                                    for(int i =0; i<invoiceDetail.snackOrder.items.size(); i++){
                                        invoiceDetail.snackItems.add(String.format("%s,%s,%s", snacks.get(i).name, invoiceDetail.snackOrder.items.get(i).quantity, snacks.get(i).price));
                                    }

                                    callback.onSuccess(invoiceDetail);
                                })
                                .addOnFailureListener(e -> {
                                    e.printStackTrace();
                                    callback.onSuccess(invoiceDetail);
                                    callback.onError(e.getMessage());
                                });
                        }
                        else{
                            callback.onSuccess(invoiceDetail);
                        }
                    })
                    .addOnFailureListener(e -> {
                        callback.onError(e.toString());
                        e.printStackTrace();
                    });
    }



}
