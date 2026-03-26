package com.flag.bozi

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.flag.bozi.databinding.ActivitySettingsBinding


class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("quiz_prefs", MODE_PRIVATE)
        val isSoundEnabled = prefs.getBoolean("sound_enabled", true)
        binding.switchSound.isChecked = isSoundEnabled

        // При включении/выключении звука
        binding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("sound_enabled", isChecked).apply()
            if (isChecked) {
                MusicManager.start(this)
            } else {
                MusicManager.stop()
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (prefs.getBoolean("sound_enabled", true)) {
            MusicManager.start(this)
        }
    }

    override fun onPause() {
        super.onPause()
        MusicManager.stop()
    }
}
