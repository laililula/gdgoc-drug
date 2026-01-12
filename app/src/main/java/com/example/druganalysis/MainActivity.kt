package com.example.druganalysis

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.druganalysis.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 최초 홈 Fragment 세팅
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, HomeFragment())
                .commit()
        }

        // BottomNavigationView → binding으로 접근
        binding.bottomNavigation.setOnItemSelectedListener { item ->

            val fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_history -> SearchFragment()
                R.id.nav_medicine -> MyMedicineFragment()
                else -> null
            }

            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(binding.fragmentContainer.id, it)
                    .commit()
                true
            } ?: false
        }
    }
}