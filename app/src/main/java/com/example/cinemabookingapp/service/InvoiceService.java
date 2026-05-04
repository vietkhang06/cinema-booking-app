package com.example.cinemabookingapp.service;

import android.util.Log;

import com.example.cinemabookingapp.core.constants.FirestoreCollections;
import com.example.cinemabookingapp.domain.model.Booking;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class InvoiceService {
    public static final String logTag = "Invoice Service";
    FirebaseFirestore firestore;
    public InvoiceService(){
        firestore = FirebaseFirestore.getInstance();
    }
    public void updatePaymentStatus(String invoiceId, String status){
        
    }

    public Booking getInvoiceFromId(String invoiceId){
        try {
            Task<DocumentSnapshot> getInvoiceTask = FirebaseFirestore.getInstance().collection(FirestoreCollections.BOOKINGS)
                    .document(invoiceId).get();
            return Tasks.await(getInvoiceTask).toObject(Booking.class);
        }catch (Exception e){
            Log.e(logTag, e.getMessage());
            return null;
        }
    }
}
