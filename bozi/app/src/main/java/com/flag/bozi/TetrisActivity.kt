package com.flag.bozi.tetris

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.flag.bozi.MusicManager
import com.flag.bozi.R
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader

class TetrisActivity : AppCompatActivity() {

    private lateinit var tetrisView: TetrisView
    private lateinit var scoreText: TextView

    // 👉 Реклама
    private lateinit var interstitialAdLoader: InterstitialAdLoader
    private var restartClickCount = 0 // счётчик нажатий

    companion object {
        private const val INTERSTITIAL_ID = "demo-interstitial-yandex" // замени на свой ID из кабинета Яндекса
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tetris)

        // элементы UI
        tetrisView = findViewById(R.id.tetrisView)
        scoreText = findViewById(R.id.scoreText)
        val btnBack: ImageView = findViewById(R.id.btnBack)
        val btnLeft: ImageButton = findViewById(R.id.btnLeft)
        val btnRight: ImageButton = findViewById(R.id.btnRight)
        val btnRotate: ImageButton = findViewById(R.id.btnRotate)
        val btnRestart: Button = findViewById(R.id.btnRestart)

        // кнопка Назад
        btnBack.setOnClickListener { finish() }

        // управление
        btnLeft.setOnClickListener { tetrisView.moveLeft() }
        btnRight.setOnClickListener { tetrisView.moveRight() }
        btnRotate.setOnClickListener { tetrisView.rotatePiece() }

        // инициализация загрузчика рекламы
        interstitialAdLoader = InterstitialAdLoader(this)

        // кнопка "Рестарт"
        btnRestart.setOnClickListener {
            restartClickCount++
            if (restartClickCount >= 2) {
                // показываем рекламу начиная со 2-го нажатия
                loadAndShowInterstitial()
            } else {
                // первый раз — просто рестарт
                tetrisView.restartGame()
                updateScore()
            }
        }

        // обновление очков каждые 300 мс
        val updateThread = object : Thread() {
            override fun run() {
                while (!isInterrupted) {
                    runOnUiThread { updateScore() }
                    sleep(300)
                }
            }
        }
        updateThread.start()

        // Game Over
        tetrisView.onGameOver = { finalScore ->
            runOnUiThread {
                showGameOverDialog(finalScore)
            }
        }
    }

    private fun updateScore() {
        scoreText.text = "Очки: ${tetrisView.score}"

        // Сохраняем прогресс Tetris в профиле
        val prefs = getSharedPreferences("profile_prefs", MODE_PRIVATE)
        prefs.edit().putInt("tetris_score", tetrisView.score).apply()
    }

    private fun showGameOverDialog(score: Int) {
        AlertDialog.Builder(this)
            .setTitle("Игра окончена")
            .setMessage("Вы набрали $score очков")
            .setPositiveButton("Играть снова") { _, _ ->
                tetrisView.restartGame()
                updateScore()
            }
            .setNegativeButton("Назад") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    // загрузка и показ межстраничной рекламы
    private fun loadAndShowInterstitial() {
        interstitialAdLoader.setAdLoadListener(object :
            com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener {
            override fun onAdLoaded(ad: InterstitialAd) {
                ad.setAdEventListener(object : InterstitialAdEventListener {
                    override fun onAdShown() {}
                    override fun onAdDismissed() {
                        // после рекламы перезапускаем игру
                        tetrisView.restartGame()
                        updateScore()
                    }

                    override fun onAdFailedToShow(adError: com.yandex.mobile.ads.common.AdError) {
                        // если реклама не показалась — просто рестарт
                        tetrisView.restartGame()
                        updateScore()
                    }

                    override fun onAdClicked() {}
                    override fun onAdImpression(impressionData: com.yandex.mobile.ads.common.ImpressionData?) {}
                })
                ad.show(this@TetrisActivity)
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                // если реклама не загрузилась — просто рестарт
                tetrisView.restartGame()
                updateScore()
            }
        })

        interstitialAdLoader.loadAd(
            AdRequestConfiguration.Builder(INTERSTITIAL_ID).build()
        )
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
