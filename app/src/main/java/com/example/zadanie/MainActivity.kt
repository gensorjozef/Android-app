// Zadanie bolo postavene a prisposobene standardom, ktore nam poskytol pan Cavojsky.
// Zdroj https://github.com/marosc/mobv2022/tree/master

package com.example.zadanie

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.example.zadanie.databinding.ActivityMainBinding
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
//    private lateinit var bottomNavigationView: BottomNavigationView
    private val appBarConfiguration = AppBarConfiguration(
        setOf(
            R.id.login_fragment,
            R.id.sign_up_fragment,
            R.id.bars_fragment,
            R.id.locate_fragment,
            R.id.friends_fragment,
        )
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setSupportActionBar(binding.toolbar)
        binding.toolbarTitle.text = binding.toolbar.title

        supportActionBar?.setDisplayShowTitleEnabled(false)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val navController = navHostFragment.navController
        findViewById<BottomNavigationView>(R.id.nav_view)
            .setupWithNavController(navController)
//        bottomNavigationView = findViewById(R.id.nav_view)
//        bottomNavigationView.setOnItemSelectedListener {
//            when (it.itemId) {
//                R.id.locate_fragment -> {
//                    println("Klikol si location")
//                    onOptionsItemSelected(it)
//                }
//                R.id.sign_up_fragment-> {
//                    println("Klikol si friends")
//                    onOptionsItemSelected(it)
//                }
//                R.id.bars_fragment-> {
//                    println("Klikol si list")
//                    onOptionsItemSelected(it)
//                }
//            }
//            return@setOnItemSelectedListener true
//        }
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }
    fun setActionBarTitle(title: String?) {
        binding.toolbarTitle.text = title
    }
}