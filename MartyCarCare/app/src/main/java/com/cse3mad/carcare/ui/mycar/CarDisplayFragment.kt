package com.cse3mad.carcare.ui.mycar

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.cse3mad.carcare.R
import com.cse3mad.carcare.databinding.FragmentCarDisplayBinding
import com.cse3mad.carcare.utils.AuthManager
import com.cse3mad.carcare.utils.CarPreferences
import com.cse3mad.carcare.utils.MaintenancePreferences
import com.cse3mad.carcare.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.Calendar
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CarDisplayFragment : Fragment() {
    private var _binding: FragmentCarDisplayBinding? = null
    private val binding get() = _binding!!
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var maintenancePrefs: MaintenancePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This will enable us to handle the action bar
        setHasOptionsMenu(true)
        maintenancePrefs = MaintenancePreferences.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarDisplayBinding.inflate(inflater, container, false)
        
        // Initialize Firebase
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        
        // Disable back button in action bar
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        
        // Disable physical back button
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Do nothing to disable back navigation
                }
            }
        )
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMaintenanceButton()
        setupOilChangeButton()
        updateMaintenanceUI()
        updateOilChangeUI()
        setupDeleteButton()
        setupSignOutButton()
        loadCarDetails()
    }

    private fun setupMaintenanceButton() {
        binding.addReminderButton.setOnClickListener {
            showDatePicker()
        }
        
        binding.editMaintenanceDateButton.setOnClickListener {
            showDatePicker()
        }
    }

    private fun setupOilChangeButton() {
        binding.addOilChangeReminderButton.setOnClickListener {
            showOilChangeDatePicker()
        }
        
        binding.editOilChangeDateButton.setOnClickListener {
            showOilChangeDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, selectedMonth)
                    set(Calendar.DAY_OF_MONTH, selectedDay)
                    // Set time to start of day
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // Only allow future dates
                if (selectedCalendar.timeInMillis < calendar.timeInMillis) {
                    Toast.makeText(context, "Please select a future date", Toast.LENGTH_SHORT).show()
                    return@DatePickerDialog
                }

                // Save the maintenance date
                lifecycleScope.launch(Dispatchers.IO) {
                    maintenancePrefs.setMaintenanceDate(selectedCalendar)
                    withContext(Dispatchers.Main) {
                        updateMaintenanceUI()
                    }
                }
            },
            year,
            month,
            day
        ).show()
    }

    private fun showOilChangeDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, selectedMonth)
                    set(Calendar.DAY_OF_MONTH, selectedDay)
                    // Set time to start of day
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // Only allow future dates
                if (selectedCalendar.timeInMillis < calendar.timeInMillis) {
                    Toast.makeText(context, "Please select a future date", Toast.LENGTH_SHORT).show()
                    return@DatePickerDialog
                }

                // Save the oil change date
                lifecycleScope.launch(Dispatchers.IO) {
                    maintenancePrefs.setOilChangeDate(selectedCalendar)
                    withContext(Dispatchers.Main) {
                        updateOilChangeUI()
                    }
                }
            },
            year,
            month,
            day
        ).show()
    }

    private fun updateMaintenanceUI() {
        lifecycleScope.launch(Dispatchers.Main) {
            val hasDate = withContext(Dispatchers.IO) {
                maintenancePrefs.hasMaintenanceDate()
            }
            if (hasDate) {
                // Hide the add reminder button
                binding.addReminderButton.visibility = View.GONE
                
                // Show the countdown text
                val daysUntil = withContext(Dispatchers.IO) {
                    maintenancePrefs.getDaysUntilMaintenance()
                }
                val countdownText = when {
                    daysUntil > 1 -> "Your next car maintenance is in $daysUntil days."
                    daysUntil == 1 -> "Your next car maintenance is tomorrow."
                    daysUntil == 0 -> "Your car maintenance is due today!"
                    else -> {
                        // If maintenance date has passed, reset and show the button again
                        withContext(Dispatchers.IO) {
                            maintenancePrefs.clearMaintenanceDate()
                        }
                        binding.addReminderButton.visibility = View.VISIBLE
                        "Schedule your next maintenance"
                    }
                }
                binding.maintenanceStatusText.text = countdownText
                binding.maintenanceStatusText.visibility = View.VISIBLE

                // Show notification if maintenance is tomorrow or today
                when (daysUntil) {
                    1 -> showMaintenanceNotification(true)
                    0 -> showMaintenanceNotification(false)
                }
            } else {
                // Show the add reminder button and hide the status text
                binding.addReminderButton.visibility = View.VISIBLE
                binding.maintenanceStatusText.visibility = View.GONE
            }
        }
    }

    private fun showMaintenanceNotification(isTomorrow: Boolean) {
        val title = if (isTomorrow) "Maintenance Reminder" else "Maintenance Due"
        val message = if (isTomorrow) "Your car maintenance is tomorrow!" else "Your car maintenance is due today!"
        NotificationHelper.showMaintenanceReminder(requireContext(), title, message, if (isTomorrow) 1 else 2)
    }

    private fun updateOilChangeUI() {
        lifecycleScope.launch(Dispatchers.Main) {
            val hasDate = withContext(Dispatchers.IO) {
                maintenancePrefs.hasOilChangeDate()
            }
            if (hasDate) {
                // Hide the add reminder button and show status text
                binding.addOilChangeReminderButton.visibility = View.GONE
                binding.oilChangeStatusText.visibility = View.VISIBLE
                
                // Show the countdown text
                val daysUntil = withContext(Dispatchers.IO) {
                    maintenancePrefs.getDaysUntilOilChange()
                }
                val countdownText = when {
                    daysUntil > 1 -> "Your next oil change is in $daysUntil days."
                    daysUntil == 1 -> "Your next oil change is tomorrow."
                    daysUntil == 0 -> "Your oil change is due today!"
                    else -> {
                        // If oil change date has passed, reset and show the button again
                        withContext(Dispatchers.IO) {
                            maintenancePrefs.clearOilChangeDate()
                        }
                        binding.addOilChangeReminderButton.visibility = View.VISIBLE
                        binding.oilChangeStatusText.visibility = View.GONE
                        "Schedule your next oil change"
                    }
                }
                binding.oilChangeStatusText.text = countdownText

                // Show notification if oil change is tomorrow or today
                when (daysUntil) {
                    1 -> showOilChangeNotification(true)
                    0 -> showOilChangeNotification(false)
                }
            } else {
                // Show the add reminder button and hide the status text
                binding.addOilChangeReminderButton.visibility = View.VISIBLE
                binding.oilChangeStatusText.visibility = View.GONE
            }
        }
    }

    private fun showOilChangeNotification(isTomorrow: Boolean) {
        val title = if (isTomorrow) "Oil Change Reminder" else "Oil Change Due"
        val message = if (isTomorrow) "Your oil change is tomorrow!" else "Your oil change is due today!"
        NotificationHelper.showMaintenanceReminder(requireContext(), title, message, if (isTomorrow) 3 else 4)
    }

    private fun setupDeleteButton() {
        binding.deleteProfileButton.setOnClickListener {
            deleteCarProfile()
        }
    }

    private fun setupSignOutButton() {
        binding.signOutButton.setOnClickListener {
            signOut()
        }
    }

    private fun loadCarDetails() {
        arguments?.let { bundle ->
            val make = bundle.getString("make", "").trim()
            val model = bundle.getString("model", "").trim()
            val year = bundle.getString("year", "")

            // Display car name with year
            binding.carNameTextView.text = "$year $make $model"

            // Create path using make and model as entered
            val basePath = "cars/$make $model"
            val jpgPath = "$basePath.jpg"
            val pngPath = "$basePath.png"
            
            // Create Glide options for consistent image display
            val options = RequestOptions()
                .transform(CenterCrop(), RoundedCorners(16))
                .override(800, 600)
                .placeholder(R.drawable.ic_car) // Add placeholder
                .error(R.drawable.ic_car) // Add error image
            
            // Try .jpg first
            val jpgRef: StorageReference = storage.reference.child(jpgPath)
            jpgRef.downloadUrl.addOnSuccessListener { uri: Uri ->
                if (!isAdded) return@addOnSuccessListener // Check if fragment is still attached
                
                Glide.with(this)
                    .load(uri.toString())
                    .apply(options)
                    .override(1200, 800)
                    .fitCenter()
                    .into(binding.carImage)
            }.addOnFailureListener { _ ->
                if (!isAdded) return@addOnFailureListener // Check if fragment is still attached
                
                // If .jpg fails, try .png
                val pngRef: StorageReference = storage.reference.child(pngPath)
                pngRef.downloadUrl.addOnSuccessListener { uri: Uri ->
                    if (!isAdded) return@addOnSuccessListener // Check if fragment is still attached
                    
                    Glide.with(this)
                        .load(uri.toString())
                        .apply(options)
                        .override(1200, 800)
                        .fitCenter()
                        .into(binding.carImage)
                }.addOnFailureListener { _ ->
                    if (!isAdded) return@addOnFailureListener // Check if fragment is still attached
                    
                    // If both fail, just show default car image
                    Glide.with(this)
                        .load(R.drawable.ic_car)
                        .apply(options)
                        .override(1200, 800)
                        .fitCenter()
                        .into(binding.carImage)
                }
            }
        }
    }

    private fun deleteCarProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Delete car data from Firestore
            db.collection("users")
                .document(currentUser.uid)
                .collection("cars")
                .document("primary")
                .delete()
                .addOnSuccessListener {
                    // Clear all local data
                    lifecycleScope.launch(Dispatchers.IO) {
                        maintenancePrefs.clearAllReminders()
                        withContext(Dispatchers.Main) {
                            CarPreferences.clearCarDetails()
                    Toast.makeText(context, "Car profile deleted successfully", Toast.LENGTH_SHORT).show()
                    // Navigate back to car form
                    findNavController().navigate(R.id.action_carDisplayFragment_to_carDetailsFormFragment)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error deleting car profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // For guest users, clear local data and navigate to dashboard
            lifecycleScope.launch(Dispatchers.IO) {
                maintenancePrefs.clearAllReminders()
                withContext(Dispatchers.Main) {
                    CarPreferences.clearCarDetails()
                    Toast.makeText(context, "Car profile deleted successfully", Toast.LENGTH_SHORT).show()
                    // Navigate back to dashboard instead of car form
                    findNavController().navigate(R.id.action_carDisplayFragment_to_myCarDashboardFragment)
                }
            }
        }
    }

    private fun signOut() {
        // Sign out using AuthManager
        AuthManager.signOut()
        
        // Navigate back to sign in screen
        findNavController().navigate(R.id.action_carDisplayFragment_to_navigation_my_car)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 