package com.flag.bozi

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.*

class GameActivity : AppCompatActivity() {

    private lateinit var btnA: Button
    private lateinit var btnB: Button
    private lateinit var btnC: Button
    private lateinit var btnD: Button
    private lateinit var tvQuestion: TextView
    private lateinit var tvPrize: TextView
    private lateinit var btnFifty: ImageButton
    private lateinit var btnAudience: ImageButton
    private lateinit var btnCall: ImageButton
    private lateinit var btnChange: ImageButton
    private lateinit var bannerAdView: BannerAdView

    private var currentQuestionIndex = 0
    private var isFirstHintUse = true
    private var score = 0
    private var interstitialAd: InterstitialAd? = null
    private lateinit var shuffledQuestions: List<Question>

    private val prizeLevels = listOf(
        500, 1000, 2000, 3000, 5000, 7500, 10000, 12500,
        15000, 25000, 50000, 100000, 250000, 500000, 1000000
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        initViews()
        setupBannerAd()
        loadInterstitialAd()
        setupListeners()

        shuffledQuestions = QuestionData.allQuestions.shuffled()
        showQuestion()
    }

    private fun initViews() {
        btnA = findViewById(R.id.btnA)
        btnB = findViewById(R.id.btnB)
        btnC = findViewById(R.id.btnC)
        btnD = findViewById(R.id.btnD)
        tvQuestion = findViewById(R.id.tvQuestion)
        tvPrize = findViewById(R.id.tvPrize)
        btnFifty = findViewById(R.id.btnFifty)
        btnAudience = findViewById(R.id.btnAudience)
        btnCall = findViewById(R.id.btnCall)
        btnChange = findViewById(R.id.btnChange)
        bannerAdView = findViewById(R.id.bannerAdView)
    }

    private fun setupBannerAd() {
        bannerAdView.setAdUnitId("demo-banner-yandex")
        bannerAdView.setAdSize(BannerAdSize.stickySize(this, 320))
        bannerAdView.loadAd(AdRequest.Builder().build())
    }

    private fun loadInterstitialAd() {
        val loader = InterstitialAdLoader(this)
        loader.setAdLoadListener(object : InterstitialAdLoadListener {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
            }

            override fun onAdFailedToLoad(p0: AdRequestError) {
                interstitialAd = null
            }
        })
        loader.loadAd(
            AdRequestConfiguration.Builder("demo-interstitial-yandex").build()
        )
    }

    private fun showInterstitialThen(action: () -> Unit) {
        if (interstitialAd != null) {
            interstitialAd?.setAdEventListener(object : InterstitialAdEventListener {
                override fun onAdDismissed() = action()
                override fun onAdFailedToShow(p0: AdError) = action()
                override fun onAdShown() {}
                override fun onAdClicked() {}
                override fun onAdImpression(p0: ImpressionData?) {}
            })
            interstitialAd?.show(this)
        } else {
            action()
        }
    }

    private fun showInterstitialIfNeeded(after: () -> Unit) {
        if (isFirstHintUse) {
            isFirstHintUse = false
            after()
        } else {
            showInterstitialThen(after)
        }
    }

    private fun setupListeners() {
        btnA.setOnClickListener { checkAnswer(0) }
        btnB.setOnClickListener { checkAnswer(1) }
        btnC.setOnClickListener { checkAnswer(2) }
        btnD.setOnClickListener { checkAnswer(3) }

        btnFifty.setOnClickListener { showInterstitialIfNeeded { useFiftyFifty() } }
        btnAudience.setOnClickListener { showInterstitialIfNeeded { showAudienceHelp() } }
        btnCall.setOnClickListener { showInterstitialIfNeeded { showCallFriend() } }
        btnChange.setOnClickListener { showInterstitialIfNeeded { skipQuestion() } }
    }

    private fun showQuestion() {
        if (currentQuestionIndex >= shuffledQuestions.size) {
            Toast.makeText(this, "Поздравляем! Вы выиграли 1 000 000 ₽", Toast.LENGTH_LONG).show()
            showGameOverDialog()
            return
        }

        val q = shuffledQuestions[currentQuestionIndex]
        tvPrize.text = "${prizeLevels[currentQuestionIndex]} ₽"
        tvQuestion.text = q.text

        btnA.text = "A: ${q.answers[0]}"
        btnB.text = "B: ${q.answers[1]}"
        btnC.text = "C: ${q.answers[2]}"
        btnD.text = "D: ${q.answers[3]}"

        listOf(btnA, btnB, btnC, btnD).forEach {
            it.visibility = View.VISIBLE
            it.setBackgroundResource(R.drawable.answer_button_default)
            it.isEnabled = true
        }
    }

    private fun checkAnswer(selectedIndex: Int) {
        val q = shuffledQuestions[currentQuestionIndex]
        val buttons = listOf(btnA, btnB, btnC, btnD)

        buttons.forEach { it.isEnabled = false }

        buttons[selectedIndex].setBackgroundResource(
            if (selectedIndex == q.correctIndex) R.drawable.answer_button_correct
            else R.drawable.answer_button_wrong
        )

        if (selectedIndex == q.correctIndex) {
            // начисляем очки за этот вопрос
            score += prizeLevels[currentQuestionIndex]

            // сохраняем прогресс в SharedPreferences для профиля
            val prefs = getSharedPreferences("profile_prefs", MODE_PRIVATE)
            prefs.edit().putInt("millioner_today", score).apply()

            currentQuestionIndex++
            buttons[selectedIndex].postDelayed({
                showQuestion()
            }, 1500)
        } else {
            showGameOverDialog()
        }
    }

    private fun useFiftyFifty() {
        val correctIndex = shuffledQuestions[currentQuestionIndex].correctIndex
        val buttons = listOf(btnA, btnB, btnC, btnD)
        val hidden = buttons.indices.filter { it != correctIndex }.shuffled().take(2)
        hidden.forEach { buttons[it].visibility = View.INVISIBLE }
    }

    private fun showAudienceHelp() {
        val correctAnswer = shuffledQuestions[currentQuestionIndex].answers[
            shuffledQuestions[currentQuestionIndex].correctIndex
        ]
        AlertDialog.Builder(this)
            .setTitle("Мнение зала")
            .setMessage("Зал считает, что правильный ответ: $correctAnswer")
            .setPositiveButton("ОК", null)
            .show()
    }

    private fun showCallFriend() {
        val q = shuffledQuestions[currentQuestionIndex]
        val isCorrect = (0..100).random() <= 80
        val answer = if (isCorrect) q.answers[q.correctIndex]
        else q.answers.filterIndexed { i, _ -> i != q.correctIndex }.random()

        AlertDialog.Builder(this)
            .setTitle("Звонок другу")
            .setMessage("Я думаю, правильный ответ: $answer")
            .setPositiveButton("Спасибо", null)
            .show()
    }

    private fun skipQuestion() {
        currentQuestionIndex++
        showQuestion()
    }

    private fun showGameOverDialog() {
        AlertDialog.Builder(this)
            .setTitle("Игра окончена")
            .setMessage("Хотите сыграть снова?")
            .setCancelable(false)
            .setPositiveButton("Начать") { _, _ ->
                showInterstitialThen {
                    restartGame()
                }
            }
            .setNegativeButton("Выход") { _, _ -> finish() }
            .show()
    }

    private fun restartGame() {
        shuffledQuestions = QuestionData.allQuestions.shuffled()
        currentQuestionIndex = 0
        score = 0

        // сброс дневного прогресса
        val prefs = getSharedPreferences("profile_prefs", MODE_PRIVATE)
        prefs.edit().putInt("millioner_today", 0).apply()

        isFirstHintUse = true
        showQuestion()
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
