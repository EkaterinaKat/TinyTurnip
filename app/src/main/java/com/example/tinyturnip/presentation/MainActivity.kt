package com.example.tinyturnip.presentation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.tinyturnip.R
import com.example.tinyturnip.presentation.acts.ActsFragment
import com.example.tinyturnip.presentation.charts.ChartsFragment
import com.example.tinyturnip.presentation.home.HomeFragment
import com.example.tinyturnip.presentation.logs.LogsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Bottom inset is handled by BottomNavigationView itself
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> showFragment(HomeFragment())
                R.id.nav_acts -> showFragment(ActsFragment())
                R.id.nav_logs -> showFragment(LogsFragment())
                R.id.nav_charts -> showFragment(ChartsFragment())
                else -> return@setOnItemSelectedListener false
            }
            true
        }
        bottomNavigation.setOnItemReselectedListener { }

        if (savedInstanceState == null) {
            showFragment(HomeFragment())
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
