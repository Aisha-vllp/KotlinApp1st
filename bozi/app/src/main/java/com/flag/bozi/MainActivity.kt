package com.flag.bozi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.flag.bozi.R
import com.flag.bozi.databinding.ActivityMainBinding


const val REWARDED_ID = "demo-rewarded-yandex"
const val INTERSTITIAL_ID = "demo-interstitial-yandex"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Отображаем главный фрагмент при старте
        switchFragment(HomeFragment())

        // Навигация нижнего меню
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> switchFragment(HomeFragment())
                R.id.menu_games -> switchFragment(GamesFragment())
                R.id.profil -> switchFragment(ProfileFragment())


                else -> false
            }
            true
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment)
            .commit()
    }
    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("quiz_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("sound_enabled", true)) {
            MusicManager.start(this)
        }
    }

    override fun onPause() {
        super.onPause()
        MusicManager.stop()
    }
}
