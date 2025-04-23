package com.cse3mad.carcare.ui.mechanic

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.cse3mad.carcare.R
import com.cse3mad.carcare.databinding.FragmentMechanicBinding
import java.io.IOException

class MechanicFragment : Fragment(), OnMapReadyCallback {
    private val TAG = "MechanicFragment"
    private var _binding: FragmentMechanicBinding? = null
    private val binding get() = _binding!!
    
    private var mMap: GoogleMap? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private val db = FirebaseFirestore.getInstance()
    private var geocoder: Geocoder? = null
    
    private val LOCATION_PERMISSION_REQUEST = 1
    private val DEFAULT_ZOOM = 12f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMechanicBinding.inflate(inflater, container, false)
        
        try {
            // Initialize location services
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            geocoder = Geocoder(requireContext())
            
            // Setup search input
            setupSearchInput()
            
            // Initialize map
            initializeMap()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreateView", e)
            Toast.makeText(context, "Error initializing map", Toast.LENGTH_SHORT).show()
        }
        
        return binding.root
    }

    private fun initializeMap() {
        try {
            val mapFragment = childFragmentManager
                .findFragmentById(R.id.map) as? SupportMapFragment
            mapFragment?.getMapAsync(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing map fragment", e)
            Toast.makeText(context, "Error initializing map", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSearchInput() {
        binding.searchInput.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val searchText = textView.text.toString()
                if (searchText.isNotEmpty()) {
                    searchLocation(searchText)
                    true
                } else {
                    Toast.makeText(context, "Please enter a location", Toast.LENGTH_SHORT).show()
                    false
                }
            } else {
                false
            }
        }
    }

    private fun searchLocation(location: String) {
        try {
            val addresses = geocoder?.getFromLocationName(location, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val latLng = LatLng(address.latitude, address.longitude)
                mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
                fetchNearbyMechanics(latLng)
            } else {
                Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error searching location", e)
            Toast.makeText(context, "Error searching location", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        try {
            mMap = googleMap
            
            // Set default map type
            mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            
            // Apply custom map style
            val success = mMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            ) ?: false
            
            if (!success) {
                Log.e(TAG, "Style parsing failed")
            }
            
            // Configure UI settings
            mMap?.uiSettings?.apply {
                isZoomControlsEnabled = true
                isCompassEnabled = true
                isMyLocationButtonEnabled = true
                isMapToolbarEnabled = true
            }
            
            // Set initial camera position (default to a known location if no permission)
            val defaultLocation = LatLng(51.5074, -0.1278) // London
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
            
            // Check for location permission
            if (checkLocationPermission()) {
                setupMap()
            } else {
                requestLocationPermission()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up map", e)
            Toast.makeText(context, "Error setting up map", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupMap() {
        try {
            mMap?.isMyLocationEnabled = true
            
            // Get current location
            fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM))
                    fetchNearbyMechanics(currentLatLng)
                }
            }?.addOnFailureListener { e ->
                Log.e(TAG, "Error getting location", e)
                Toast.makeText(context, "Error getting location", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Error setting up location", e)
            Toast.makeText(context, "Error setting up location", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun fetchNearbyMechanics(currentLocation: LatLng) {
        mMap?.clear()
        
        db.collection("mechanics")
            .get()
            .addOnSuccessListener { documents ->
                val bounds = LatLngBounds.builder()
                var markersAdded = false
                
                for (document in documents) {
                    val lat = document.getDouble("latitude") ?: continue
                    val lng = document.getDouble("longitude") ?: continue
                    val name = document.getString("name") ?: continue
                    
                    val mechanicLocation = LatLng(lat, lng)
                    if (isWithinRadius(currentLocation, mechanicLocation, 10.0)) {
                        mMap?.addMarker(
                            MarkerOptions()
                                .position(mechanicLocation)
                                .title(name)
                        )
                        bounds.include(mechanicLocation)
                        markersAdded = true
                    }
                }
                
                if (markersAdded) {
                    bounds.include(currentLocation)
                    mMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching mechanics", e)
                Toast.makeText(context, "Error fetching mechanics", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun isWithinRadius(from: LatLng, to: LatLng, radiusKm: Double): Boolean {
        val R = 6371.0 // Earth's radius in km
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)
        
        val a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLng/2) * Math.sin(dLng/2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
        val distance = R * c
        
        return distance <= radiusKm
    }
    
    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupMap()
                } else {
                    Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mMap = null
        fusedLocationClient = null
        geocoder = null
    }
} 