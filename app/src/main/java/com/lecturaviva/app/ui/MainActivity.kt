package com.lecturaviva.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.lecturaviva.app.R
import com.lecturaviva.app.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHost.navController

        //para ocultar barra inferior en BookDetail
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.visibility = when (destination.id) {
                R.id.bookDetailFragment -> android.view.View.GONE
                else -> android.view.View.VISIBLE
            }
        }


        binding.bottomNav.setOnItemSelectedListener { item ->
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, false)
                .setLaunchSingleTop(true)
                .build()
            try {
                navController.navigate(item.itemId, null, navOptions)
            } catch (e: Exception) {
                // destino no encontrado — ignorar
            }
            true
        }


        binding.bottomNav.setOnItemReselectedListener { item ->
            val navOptions = NavOptions.Builder()
                .setPopUpTo(item.itemId, true)
                .setLaunchSingleTop(true)
                .build()
            try {
                navController.navigate(item.itemId, null, navOptions)
            } catch (e: Exception) { }
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()
}