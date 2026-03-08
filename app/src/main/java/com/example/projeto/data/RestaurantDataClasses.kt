package com.example.projeto.data

data class Restaurant(
    val id: String,
    val name: String,
    val rating: Double,
    val distance: String,
    val description: String,
    val imageUrl: String?,
    val address: String,
    val phoneNumber: String?,
    val isOpen: Boolean,
    val priceLevel: Int?,
    val placeId: String
)

data class PlacePhoto(
    val photoReference: String,
    val width: Int,
    val height: Int
)

data class OpeningHours(
    val openNow: Boolean,
    val weekdayText: List<String>
)

// API Response models
data class NearbySearchResponse(
    val results: List<PlaceResult>,
    val status: String,
    val nextPageToken: String?
)

data class PlaceResult(
    val placeId: String,
    val name: String,
    val rating: Double?,
    val priceLevel: Int?,
    val vicinity: String,
    val geometry: Geometry,
    val photos: List<PlacePhoto>?,
    val openingHours: OpeningHours?
)

data class Geometry(
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)