package com.cse3mad.carcare

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.cse3mad.carcare.databinding.ActivityMainBinding
import com.cse3mad.carcare.utils.ThemeManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var navView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize theme from saved preference
        ThemeManager.applyTheme(ThemeManager.isDarkMode(this))
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)

        navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_activity_main)
        
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_mechanic,
                R.id.navigation_guides,
                R.id.navigation_my_car
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        
        // Set up bottom navigation with custom navigation handling
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home)
                    true
                }
                R.id.navigation_mechanic -> {
                    navController.navigate(R.id.navigation_mechanic)
                    true
                }
                R.id.navigation_guides -> {
                    navController.navigate(R.id.navigation_guides)
                    true
                }
                R.id.navigation_my_car -> {
                    navController.navigate(R.id.navigation_my_car)
                    true
                }
                else -> false
            }
        }

        // Add destination change listener
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home -> navView.menu.findItem(R.id.navigation_home)?.isChecked = true
                R.id.navigation_mechanic -> navView.menu.findItem(R.id.navigation_mechanic)?.isChecked = true
                R.id.navigation_guides -> navView.menu.findItem(R.id.navigation_guides)?.isChecked = true
                R.id.navigation_my_car -> navView.menu.findItem(R.id.navigation_my_car)?.isChecked = true
                else -> {
                    // For other destinations, find the parent destination and select it
                    val parentDestination = navController.graph.findNode(destination.id)?.parent?.id
                    if (parentDestination != null) {
                        navView.menu.findItem(parentDestination)?.isChecked = true
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}