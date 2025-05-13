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
        navView.setupWithNavController(navController)

        // Add destination change listener
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home,
                R.id.navigation_mechanic,
                R.id.navigation_guides,
                R.id.navigation_my_car -> {
                    navView.menu.findItem(destination.id)?.isChecked = true
                }
                R.id.myCarDashboardFragment,
                R.id.carDetailsFormFragment,
                R.id.carDisplayFragment -> {
                    // These are part of the My Car flow, so select the My Car tab
                    navView.menu.findItem(R.id.navigation_my_car)?.isChecked = true
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}