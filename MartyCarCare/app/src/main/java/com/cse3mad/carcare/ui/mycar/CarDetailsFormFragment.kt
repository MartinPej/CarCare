package com.cse3mad.carcare.ui.mycar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cse3mad.carcare.R
import com.cse3mad.carcare.databinding.FragmentCarDetailsFormBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class CarDetailsFormFragment : Fragment() {
    private var _binding: FragmentCarDetailsFormBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val carMakes = listOf("") + listOf(
        "Toyota", "Honda", "Ford", "Nissan", "Hyundai", "Kia",
        "BMW", "Mercedes-Benz", "Audi", "Volkswagen", "Mazda", "Subaru"
    ).sorted()

    private val modelsByMake = mapOf(
        "" to listOf(""),
        "Toyota" to listOf("Camry", "C-HR", "Corolla", "GR Yaris", "HiLux", "Kluger", "LandCruiser", "Prado", "RAV4", "Yaris Cross"),
        "Honda" to listOf("Civic", "CR-V", "HR-V"),
        "Ford" to listOf("Everest", "Puma", "Ranger"),
        "Nissan" to listOf("Leaf", "Navara", "Patrol", "Qashqai", "X-Trail"),
        "Hyundai" to listOf("i20 N", "i30", "Ioniq 5", "Kona", "Santa Fe", "Tucson", "Venue"),
        "Kia" to listOf("Carnival", "Cerato", "EV6", "Niro EV", "Seltos", "Sportage"),
        "BMW" to listOf("1 Series", "2 Series", "X3"),
        "Mercedes-Benz" to listOf("A-Class", "GLC", "GLE"),
        "Audi" to listOf("A3", "Q5"),
        "Volkswagen" to listOf("Amarok", "Tiguan", "T-Roc"),
        "Mazda" to listOf("2", "BT-50", "CX-3", "CX-5", "CX-30", "MX-50", "6"),
        "Subaru" to listOf("Crosstrek", "Forester", "Outback", "WRX", "XV")
    )

    private val years = listOf("") + (2024 downTo 1990).map { it.toString() }

    private var selectedMake: String? = null
    private var selectedModel: String? = null
    private var selectedYear: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarDetailsFormBinding.inflate(inflater, container, false)
        
        // Initialize Firebase Auth and Firestore
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSpinners()
        setupConfirmButton()
        loadSavedCarDetails()
    }

    private fun loadSavedCarDetails() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users")
                .document(currentUser.uid)
                .collection("cars")
                .document("primary") // Using "primary" as we're storing one car per user for now
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Get saved car details
                        val make = document.getString("make")
                        val model = document.getString("model")
                        val year = document.getString("year")

                        // Set spinners to saved values
                        if (!make.isNullOrEmpty()) {
                            val makePosition = carMakes.indexOf(make)
                            if (makePosition != -1) {
                                binding.makeSpinner.setSelection(makePosition)
                                selectedMake = make
                                
                                // Update model spinner and set selection
                                updateModelSpinner(make)
                                if (!model.isNullOrEmpty()) {
                                    val models = modelsByMake[make] ?: listOf("")
                                    val modelPosition = models.indexOf(model)
                                    if (modelPosition != -1) {
                                        binding.modelSpinner.setSelection(modelPosition)
                                        selectedModel = model
                                    }
                                }
                            }
                        }

                        if (!year.isNullOrEmpty()) {
                            val yearPosition = years.indexOf(year)
                            if (yearPosition != -1) {
                                binding.yearSpinner.setSelection(yearPosition)
                                selectedYear = year
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error loading car details: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupSpinners() {
        // Setup Make Spinner
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            carMakes
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.makeSpinner.adapter = adapter
        }

        // Setup initial empty model spinner
        binding.modelSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listOf("")
        )

        // Setup Year Spinner
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            years
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.yearSpinner.adapter = adapter
        }

        // Make Spinner listener
        binding.makeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMake = carMakes[position]
                updateModelSpinner(selectedMake)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedMake = null
            }
        }

        // Model Spinner listener
        binding.modelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedModel = binding.modelSpinner.selectedItem as String?
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedModel = null
            }
        }

        // Year Spinner listener
        binding.yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedYear = years[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedYear = null
            }
        }
    }

    private fun updateModelSpinner(make: String?) {
        val models = if (make.isNullOrEmpty()) listOf("") else modelsByMake[make] ?: listOf("")
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            models
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.modelSpinner.adapter = adapter
        }
    }

    private fun setupConfirmButton() {
        binding.confirmButton.setOnClickListener {
            if (selectedMake.isNullOrEmpty() || selectedModel.isNullOrEmpty() || selectedYear.isNullOrEmpty()) {
                Toast.makeText(context, "Please select all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = auth.currentUser
            if (currentUser != null) {
                // Create car data map
                val carData = hashMapOf(
                    "make" to selectedMake,
                    "model" to selectedModel,
                    "year" to selectedYear,
                    "updatedAt" to System.currentTimeMillis()
                )

                // Save to Firestore
                db.collection("users")
                    .document(currentUser.uid)
                    .collection("cars")
                    .document("primary") // Using "primary" as we're storing one car per user for now
                    .set(carData)
                    .addOnSuccessListener {
                        // Create bundle for navigation
                        val bundle = Bundle().apply {
                            putString("make", selectedMake)
                            putString("model", selectedModel)
                            putString("year", selectedYear)
                        }

                        // Navigate to display fragment
                        findNavController().navigate(
                            R.id.action_carDetailsFormFragment_to_carDisplayFragment,
                            bundle
                        )
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error saving car details: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // If user is not logged in, just navigate without saving
                val bundle = Bundle().apply {
                    putString("make", selectedMake)
                    putString("model", selectedModel)
                    putString("year", selectedYear)
                }

                findNavController().navigate(
                    R.id.action_carDetailsFormFragment_to_carDisplayFragment,
                    bundle
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 