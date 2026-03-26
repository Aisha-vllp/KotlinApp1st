package com.flag.bozi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flag.bozi.data.QuestionsRepository
import com.flag.bozi.databinding.ActivityQuestionBinding
import com.flag.bozi.model.Question
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.rewarded.*
import com.yandex.mobile.ads.interstitial.*

class QuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuestionBinding
    private lateinit var questionList: List<Question>
    private var currentIndex = 0
    private var score = 0
    private var currentLevel = 1
    private lateinit var difficulty: String

    private val maxProgress = 1000

    // 👉 Реклама
    private lateinit var rewardedAdLoader: RewardedAdLoader
    private lateinit var interstitialAdLoader: InterstitialAdLoader

    companion object {
        private const val REWARDED_ID = "demo-rewarded-yandex"     // ⚠️ замени на свой ID
        private const val INTERSTITIAL_ID = "demo-interstitial-yandex" // ⚠️ замени на свой ID

        fun start(context: Context, difficulty: String, level: Int) {
            val intent = Intent(context, QuestionActivity::class.java)
            intent.putExtra("difficulty", difficulty)
            intent.putExtra("level", level)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        difficulty = intent.getStringExtra("difficulty") ?: "easy"
        currentLevel = intent.getIntExtra("level", 1)
        binding.tvLevel.text = "Уровень $currentLevel"

        loadQuestions()
        updateDiamondCount()
        showQuestion()
        updateProgress()

        binding.btnBack.setOnClickListener { finish() }

        // --- RewardedAd для кнопки Подсказки ---
        rewardedAdLoader = RewardedAdLoader(this)
        binding.btnHelp.setOnClickListener {
            rewardedAdLoader.setAdLoadListener(object : RewardedAdLoadListener {
                override fun onAdFailedToLoad(error: AdRequestError) {
                    Toast.makeText(this@QuestionActivity, "Реклама не доступна", Toast.LENGTH_SHORT).show()
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    ad.setAdEventListener(object : RewardedAdEventListener {
                        override fun onAdShown() {}
                        override fun onRewarded(reward: Reward) {
                            // ✅ Награда — кристаллы
                            addDiamonds(50)
                            updateDiamondCount()
                            Toast.makeText(this@QuestionActivity, "+50 кристаллов!", Toast.LENGTH_SHORT).show()
                        }
                        override fun onAdFailedToShow(adError: AdError) {}
                        override fun onAdDismissed() {}
                        override fun onAdClicked() {}
                        override fun onAdImpression(impressionData: ImpressionData?) {}
                    })
                    ad.show(this@QuestionActivity)
                }
            })
            rewardedAdLoader.loadAd(
                AdRequestConfiguration.Builder(REWARDED_ID).build()
            )
        }

        // --- InterstitialAd для уровней ---
        interstitialAdLoader = InterstitialAdLoader(this)
    }

    private fun loadQuestions() {
        questionList = QuestionsRepository.getQuestions(difficulty, currentLevel).shuffled()
        currentIndex = 0
        score = 0
    }

    private fun showQuestion() {
        if (currentIndex >= questionList.size) {
            goToNextLevel()
            return
        }

        val question = questionList[currentIndex]
        binding.tvQuestion.text = question.text

        val options = listOf(
            binding.btnOption1,
            binding.btnOption2,
            binding.btnOption3,
            binding.btnOption4
        )

        // сброс состояния кнопок
        options.forEach { button ->
            button.setBackgroundResource(R.drawable.answer_button_default)
            button.isEnabled = true
        }

        options.forEachIndexed { index, button ->
            button.text = question.options[index]
            button.setOnClickListener {
                options.forEach { it.isEnabled = false }

                if (index == question.correctIndex) {
                    button.setBackgroundResource(R.drawable.answer_button_correct)
                    score++
                    addDiamonds(5)
                    updateDiamondCount()
                } else {
                    button.setBackgroundResource(R.drawable.answer_button_wrong)
                    options[question.correctIndex].setBackgroundResource(R.drawable.answer_button_correct)
                }

                button.postDelayed({
                    currentIndex++
                    updateProgress()
                    showQuestion()
                }, 1000)
            }
        }
    }

    private fun goToNextLevel() {
        if (currentLevel < 20) {
            loadAndShowInterstitial {
                currentLevel++
                binding.tvLevel.text = "Уровень $currentLevel"
                loadQuestions()
                showQuestion()
                updateProgress()
            }
        } else {
            finish()
        }
    }

    private fun loadAndShowInterstitial(onFinish: () -> Unit) {
        interstitialAdLoader.setAdLoadListener(object : InterstitialAdLoadListener {
            override fun onAdLoaded(ad: InterstitialAd) {
                ad.setAdEventListener(object : InterstitialAdEventListener {
                    override fun onAdShown() {}
                    override fun onAdDismissed() { onFinish() }
                    override fun onAdFailedToShow(adError: AdError) { onFinish() }
                    override fun onAdClicked() {}
                    override fun onAdImpression(impressionData: ImpressionData?) {}
                })
                ad.show(this@QuestionActivity)
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                onFinish()
            }
        })

        interstitialAdLoader.loadAd(
            AdRequestConfiguration.Builder(INTERSTITIAL_ID).build()
        )
    }

    private fun updateProgress() {
        val progress =
            if (questionList.isNotEmpty()) (currentIndex * 100) / questionList.size else 0
        binding.progressBar.progress = progress.coerceAtMost(100)
    }

    private fun addDiamonds(amount: Int) {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val current = prefs.getInt("diamond_progress", 0)
        val newProgress = (current + amount).coerceAtMost(maxProgress)
        prefs.edit().putInt("diamond_progress", newProgress).apply()
    }

    private fun updateDiamondCount() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val diamonds = prefs.getInt("diamond_progress", 0)
        binding.tvDiamondCount.text = diamonds.toString()
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
