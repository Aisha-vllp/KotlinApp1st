package com.flag.bozi

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.flag.bozi.databinding.ActivitySplashBinding
import kotlin.jvm.java

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())
    private var progress = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        simulateLoading()
    }

    private fun simulateLoading() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                progress += 2
                binding.progressBar.progress = progress

                if (progress < 100) {
                    handler.postDelayed(this, 40)
                } else {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            }
        }, 40)
    }
}
