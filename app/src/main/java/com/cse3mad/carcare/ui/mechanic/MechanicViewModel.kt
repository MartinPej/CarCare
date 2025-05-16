package com.cse3mad.carcare.ui.mechanic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place

data class MechanicInfo(
    val place: Place,
    val distance: Double,
    val latLng: LatLng
)

class MechanicViewModel : ViewModel() {
    private val _nearbyMechanics = MutableLiveData<List<MechanicInfo>>()
    val nearbyMechanics: LiveData<List<MechanicInfo>> = _nearbyMechanics

    private val _isSidebarOpen = MutableLiveData<Boolean>()
    val isSidebarOpen: LiveData<Boolean> = _isSidebarOpen

    private val _searchLocation = MutableLiveData<LatLng>()
    val searchLocation: LiveData<LatLng> = _searchLocation

    init {
        _isSidebarOpen.value = false
        _nearbyMechanics.value = emptyList()
    }

    fun updateNearbyMechanics(mechanics: List<MechanicInfo>) {
        _nearbyMechanics.value = mechanics.sortedBy { it.distance }
    }

    fun toggleSidebar() {
        _isSidebarOpen.value = _isSidebarOpen.value?.not() ?: true
    }

    fun updateSearchLocation(location: LatLng) {
        _searchLocation.value = location
    }
} 