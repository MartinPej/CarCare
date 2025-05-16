package com.cse3mad.carcare.ui.mechanic

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.gms.common.api.ApiException
import com.cse3mad.carcare.R
import com.cse3mad.carcare.databinding.FragmentMechanicBinding
import java.util.*



class MechanicFragment : Fragment(), OnMapReadyCallback {
    private val TAG = "MechanicFragment"
    private var _binding: FragmentMechanicBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MechanicViewModel by viewModels()
    private lateinit var mechanicAdapter: MechanicListAdapter

    private var mMap: GoogleMap? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var placesClient: PlacesClient? = null
    private var geocoder: Geocoder? = null
    private val markers = mutableListOf<Marker>()

    private val LOCATION_PERMISSION_REQUEST = 1
    private val DEFAULT_ZOOM = 14f

    private var lastSearchLocation: LatLng? = null
    private var isSearching = false
    private val searchBatchSize = 5
    private val processedPlaces = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMechanicBinding.inflate(inflater, container, false)

        try {
            // Initialize Places API with error handling
            try {
                val apiKey = getString(R.string.MAPS_API_KEY)
                if (!Places.isInitialized()) {
                    Places.initialize(requireContext(), apiKey)
                }
                placesClient = Places.createClient(requireContext())
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing Places API", e)
                Toast.makeText(context, "Error initializing map services. Please try again.", Toast.LENGTH_LONG).show()
                return binding.root
            }

            // Initialize location services
            try {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
                geocoder = Geocoder(requireContext())
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing location services", e)
                Toast.makeText(context, "Error initializing location services. Please try again.", Toast.LENGTH_LONG).show()
                return binding.root
            }

            setupSearchInput()
            setupSidebar()
            setupRecyclerView()

            // Initialize map with error handling
            try {
                initializeMap()
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing map", e)
                Toast.makeText(context, "Error initializing map. Please try again.", Toast.LENGTH_LONG).show()
                return binding.root
            }

            observeViewModel()

            // Check for location permission and get location immediately
            if (checkLocationPermission()) {
                getCurrentLocation()
            } else {
                requestLocationPermission()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreateView", e)
            Toast.makeText(context, "Error initializing map services. Please try again.", Toast.LENGTH_LONG).show()
        }

        return binding.root
    }

    private fun setupSidebar() {
        binding.toggleSidebarButton.setOnClickListener {
            viewModel.toggleSidebar()
        }

        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun setupRecyclerView() {
        mechanicAdapter = MechanicListAdapter { mechanicInfo ->
            // Center map on selected mechanic
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(mechanicInfo.latLng, DEFAULT_ZOOM))
            // Find and show the marker's info window
            markers.find { marker ->
                marker.position == mechanicInfo.latLng
            }?.showInfoWindow()
            // Close sidebar
            viewModel.toggleSidebar()
        }

        binding.mechanicsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mechanicAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.nearbyMechanics.observe(viewLifecycleOwner) { mechanics ->
            mechanicAdapter.submitMechanicList(mechanics)
            updateMapMarkers(mechanics)
        }

        viewModel.isSidebarOpen.observe(viewLifecycleOwner) { isOpen ->
            if (isOpen) {
                binding.drawerLayout.openDrawer(binding.mechanicsSidebar)
            } else {
                binding.drawerLayout.closeDrawer(binding.mechanicsSidebar)
            }
        }
    }

    private fun updateMapMarkers(mechanics: List<MechanicInfo>) {
        mMap?.clear()
        markers.clear()
        mechanics.forEach { mechanic ->
            mMap?.addMarker(
                MarkerOptions()
                    .position(mechanic.latLng)
                    .title(mechanic.place.name)
                    .snippet(mechanic.place.address)
            )?.let { marker ->
                markers.add(marker)
            }
        }
    }

    private fun initializeMap() {
        try {
            val mapFragment = childFragmentManager
                .findFragmentById(R.id.map) as? SupportMapFragment
            if (mapFragment == null) {
                throw IllegalStateException("Map fragment not found")
            }
            mapFragment.getMapAsync(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing map fragment", e)
            throw e
        }
    }

    private fun setupSearchInput() {
        binding.searchInput.hint = "Search for mechanics nearby..."
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

    private fun searchNearbyRestaurants(location: LatLng) {
        // Don't search if we're already searching or if it's the same location
        if (isSearching || lastSearchLocation == location) {
            return
        }

        isSearching = true
        lastSearchLocation = location
        processedPlaces.clear()

        try {
            if (location.latitude == 0.0 && location.longitude == 0.0) {
                Toast.makeText(context, "Invalid location coordinates", Toast.LENGTH_SHORT).show()
                isSearching = false
                return
            }

            // Define the place fields we want to get
            val placeFields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS,
                Place.Field.RATING,
                Place.Field.USER_RATINGS_TOTAL,
                Place.Field.BUSINESS_STATUS,
                Place.Field.TYPES,
                Place.Field.OPENING_HOURS,
                Place.Field.PHONE_NUMBER
            )

            // Create a location bias (approximately 5km radius)
            val bounds = RectangularBounds.newInstance(
                LatLng(location.latitude - 0.045, location.longitude - 0.045),
                LatLng(location.latitude + 0.045, location.longitude + 0.045)
            )

            // List of search terms with their corresponding types
            val searchTerms = listOf(
                SearchTerm("mechanic", "car_repair"),
                SearchTerm("car service", "car_repair"),
                SearchTerm("auto repair", "car_repair"),
                SearchTerm("automotive", "car_repair"),
                SearchTerm("auto service", "car_repair")
            )

            val allPlaces = mutableListOf<MechanicInfo>()
            var currentBatch = 0

            fun processNextBatch() {
                if (currentBatch >= searchTerms.size) {
                    isSearching = false
                    if (allPlaces.isNotEmpty()) {
                        updateMapAndSidebar(allPlaces.sortedBy { it.distance })
                    } else {
                        activity?.runOnUiThread {
                            Toast.makeText(context, "No car service places found within 5km", Toast.LENGTH_SHORT).show()
                        }
                    }
                    return
                }

                val searchTerm = searchTerms[currentBatch]
                val request = FindAutocompletePredictionsRequest.builder()
                    .setLocationBias(bounds)
                    .setTypesFilter(listOf(searchTerm.type))
                    .setQuery(searchTerm.keyword)
                    .build()

                placesClient?.findAutocompletePredictions(request)
                    ?.addOnSuccessListener { response ->
                        val predictions = response.autocompletePredictions
                        if (predictions.isNotEmpty()) {
                            processPredictions(predictions, placeFields, location, allPlaces) {
                                currentBatch++
                                processNextBatch()
                            }
                        } else {
                            currentBatch++
                            processNextBatch()
                        }
                    }
                    ?.addOnFailureListener {
                        currentBatch++
                        processNextBatch()
                    }
            }

            processNextBatch()

        } catch (e: Exception) {
            isSearching = false
            Toast.makeText(context, "Error searching nearby car services: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processPredictions(
        predictions: List<AutocompletePrediction>,
        placeFields: List<Place.Field>,
        location: LatLng,
        allPlaces: MutableList<MechanicInfo>,
        onComplete: () -> Unit
    ) {
        var processedCount = 0
        val totalPredictions = predictions.size

        predictions.forEach { prediction ->
            if (prediction.placeId in processedPlaces) {
                processedCount++
                if (processedCount == totalPredictions) {
                    onComplete()
                }
                return@forEach
            }

            processedPlaces.add(prediction.placeId)
            val fetchPlaceRequest = FetchPlaceRequest.builder(prediction.placeId, placeFields).build()

            placesClient?.fetchPlace(fetchPlaceRequest)
                ?.addOnSuccessListener { fetchPlaceResponse ->
                    val place = fetchPlaceResponse.place
                    place.latLng?.let { latLng ->
                        val distance = calculateDistance(location, latLng)
                        if (distance <= 5000) {
                            allPlaces.add(MechanicInfo(place, distance, latLng))
                        }
                    }
                    processedCount++
                    if (processedCount == totalPredictions) {
                        onComplete()
                    }
                }
                ?.addOnFailureListener {
                    processedCount++
                    if (processedCount == totalPredictions) {
                        onComplete()
                    }
                }
        }
    }

    private fun searchLocation(location: String) {
        try {
            val addresses = geocoder?.getFromLocationName(location, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val latLng = LatLng(address.latitude, address.longitude)
                viewModel.updateSearchLocation(latLng)
                mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
                searchNearbyRestaurants(latLng)
            } else {
                Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching location", e)
            Toast.makeText(context, "Error searching location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient?.lastLocation?.addOnSuccessListener { location ->
                    if (location == null) {
                        Log.e(TAG, "Location is null")
                        Toast.makeText(context, "Unable to get current location", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    Log.d(TAG, "Got current location: ${location.latitude}, ${location.longitude}")
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM))
                    searchNearbyRestaurants(currentLatLng)
                }?.addOnFailureListener { e ->
                    Log.e(TAG, "Error getting location", e)
                    Toast.makeText(context, "Error getting location: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Error getting location", e)
            Toast.makeText(context, "Error getting location: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        try {
            mMap = googleMap

            // Set default map type
            mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL

            // Configure UI settings
            mMap?.uiSettings?.apply {
                isZoomControlsEnabled = true
                isCompassEnabled = true
                isMyLocationButtonEnabled = true
                isMapToolbarEnabled = true
            }

            // Set up marker click listener
            mMap?.setOnMarkerClickListener { marker ->
                marker.showInfoWindow()
                true
            }

            // Check for location permission and get location if map is ready
            if (checkLocationPermission()) {
                mMap?.isMyLocationEnabled = true
                getCurrentLocation()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up map", e)
            Toast.makeText(context, "Error setting up map. Please try again.", Toast.LENGTH_LONG).show()
        }
    }

    private fun calculateDistance(from: LatLng, to: LatLng): Double {
        val results = FloatArray(1)
        Location.distanceBetween(
            from.latitude, from.longitude,
            to.latitude, to.longitude,
            results
        )
        return results[0].toDouble()
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
                    getCurrentLocation()
                } else {
                    Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateMapAndSidebar(places: List<MechanicInfo>) {
        activity?.runOnUiThread {
            try {
                // Clear existing markers
                mMap?.clear()
                markers.clear()

                // Add markers for each place
                places.forEach { place ->
                    mMap?.addMarker(
                        MarkerOptions()
                            .position(place.latLng)
                            .title(place.place.name)
                            .snippet(place.place.address)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    )?.let { marker ->
                        markers.add(marker)
                    }
                }

                // Update the sidebar with new places
                viewModel.updateNearbyMechanics(places)

                // Adjust map camera to show all markers
                if (places.isNotEmpty()) {
                    try {
                        val builder = LatLngBounds.Builder()
                        places.forEach { builder.include(it.latLng) }
                        val bounds = builder.build()
                        val padding = resources.getDimensionPixelSize(R.dimen.map_padding)
                        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                        mMap?.animateCamera(cameraUpdate)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error adjusting map camera", e)
                        // Fallback to centering on the first place
                        places.firstOrNull()?.let { place ->
                            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, DEFAULT_ZOOM))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating map and sidebar", e)
                Toast.makeText(context, "Error updating map: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mMap = null
        fusedLocationClient = null
        placesClient = null
        geocoder = null
    }

    private data class SearchTerm(val keyword: String, val type: String)
}
