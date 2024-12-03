package com.example.mobile_dev_project

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

fun rentToestel(
    db: FirebaseFirestore,
    toestelId: String,
    renterId: String,
    startDate: LocalDate,
    endDate: LocalDate,
    onComplete: () -> Unit
) {
    // Convert dates to maps for Firestore storage
    val startDateMap = mapOf(
        "year" to startDate.year,
        "month" to startDate.monthValue,
        "day" to startDate.dayOfMonth
    )

    val endDateMap = mapOf(
        "year" to endDate.year,
        "month" to endDate.monthValue,
        "day" to endDate.dayOfMonth
    )

    val rental = hashMapOf(
        "toestelId" to toestelId,
        "renterId" to renterId,
        "startDate" to startDateMap,
        "endDate" to endDateMap,
        "status" to "active",
        "createdAt" to com.google.firebase.Timestamp.now()
    )

    db.collection("rentals")
        .add(rental)
        .addOnSuccessListener { documentReference -> 
            Log.d("RentToestel", "Rental stored with ID: ${documentReference.id}")
            onComplete()
        }
        .addOnFailureListener { e ->
            Log.e("RentToestel", "Error storing rental: ${e.message}")
            onComplete()
        }
}

fun cancelRental(
    db: FirebaseFirestore,
    rentalId: String,
    onComplete: () -> Unit
) {
    db.collection("rentals")
        .document(rentalId)
        .delete()
        .addOnSuccessListener { 
            onComplete()
        }
}