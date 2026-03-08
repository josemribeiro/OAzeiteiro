package com.example.projeto.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getCurrentLocation(): LocationResult = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            continuation.resume(LocationResult.Error("Location permission not granted"))
            return@suspendCancellableCoroutine
        }

        try {
            val cancellationTokenSource = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    continuation.resume(
                        LocationResult.Success(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                    )
                } else {
                    continuation.resume(LocationResult.Error("Unable to get location"))
                }
            }.addOnFailureListener { exception ->
                continuation.resume(LocationResult.Error(exception.message ?: "Location error"))
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }

        } catch (e: SecurityException) {
            continuation.resume(LocationResult.Error("Location permission denied"))
        }
    }

    suspend fun getLastKnownLocation(): LocationResult = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            continuation.resume(LocationResult.Error("Location permission not granted"))
            return@suspendCancellableCoroutine
        }

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        continuation.resume(
                            LocationResult.Success(
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                        )
                    } else {
                        continuation.resume(LocationResult.Error("No last known location"))
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(LocationResult.Error(exception.message ?: "Location error"))
                }
        } catch (e: SecurityException) {
            continuation.resume(LocationResult.Error("Location permission denied"))
        }
    }
}

sealed class LocationResult {
    data class Success(val latitude: Double, val longitude: Double) : LocationResult()
    data class Error(val message: String) : LocationResult()
}
