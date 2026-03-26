package com.flag.bozi

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import com.flag.bozi.databinding.ActivityGame2048Binding
import com.yandex.mobile.ads.common.*
import com.yandex.mobile.ads.rewarded.*
import com.yandex.mobile.ads.interstitial.*
import kotlin.math.abs
import kotlin.random.Random

class Game2048Activity : AppCompatActivity() {

    private lateinit var binding: ActivityGame2048Binding
    private lateinit var tiles: Array<Array<TileView>>

    private var score = 0
    private var best = 0

    private var startX = 0f
    private var startY = 0f
    private val swipeThreshold = 100

    private var lastState: Array<Array<Int>> = Array(4) { Array(4) { 0 } }

    private lateinit var rewardedAdLoader: RewardedAdLoader
    private lateinit var interstitialAdLoader: InterstitialAdLoader

    companion object {
        private const val REWARDED_ID = "demo-rewarded-yandex"
        private const val INTERSTITIAL_ID = "demo-interstitial-yandex"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGame2048Binding.inflate(layoutInflater)
        setContentView(binding.root)

        tiles = Array(4) { Array(4) { TileView(this) } }
        initGrid()
        addRandomTile()
        addRandomTile()

        val prefs = getSharedPreferences("profile_prefs", MODE_PRIVATE)
        best = prefs.getInt("game2048_best", 0)
        updateScore()

        binding.grid.setOnTouchListener { _, event -> handleSwipe(event) }

        rewardedAdLoader = RewardedAdLoader(this)
        binding.undoButton.setOnClickListener {
            rewardedAdLoader.setAdLoadListener(object : RewardedAdLoadListener {
                override fun onAdFailedToLoad(error: AdRequestError) {}
                override fun onAdLoaded(ad: RewardedAd) {
                    ad.setAdEventListener(object : RewardedAdEventListener {
                        override fun onAdShown() {}
                        override fun onRewarded(reward: Reward) {}
                        override fun onAdFailedToShow(adError: AdError) {}
                        override fun onAdDismissed() {
                            restorePreviousState()
                        }
                        override fun onAdClicked() {}
                        override fun onAdImpression(impressionData: ImpressionData?) {}
                    })
                    ad.show(this@Game2048Activity)
                }
            })
            rewardedAdLoader.loadAd(AdRequestConfiguration.Builder(REWARDED_ID).build())
        }

        interstitialAdLoader = InterstitialAdLoader(this)
        binding.breakTileButton.setOnClickListener {
            interstitialAdLoader.setAdLoadListener(object : InterstitialAdLoadListener {
                override fun onAdLoaded(ad: InterstitialAd) {
                    ad.setAdEventListener(object : InterstitialAdEventListener {
                        override fun onAdShown() {}
                        override fun onAdDismissed() { breakRandomTile() }
                        override fun onAdFailedToShow(adError: AdError) { breakRandomTile() }
                        override fun onAdClicked() {}
                        override fun onAdImpression(impressionData: ImpressionData?) {}
                    })
                    ad.show(this@Game2048Activity)
                }
                override fun onAdFailedToLoad(error: AdRequestError) { breakRandomTile() }
            })
            interstitialAdLoader.loadAd(AdRequestConfiguration.Builder(INTERSTITIAL_ID).build())
        }

        binding.restartButton.setOnClickListener { restartGame() }
        binding.backButton.setOnClickListener { finish() }
    }

    private fun initGrid() {
        val grid = binding.grid
        grid.removeAllViews()
        for (i in 0..3) {
            for (j in 0..3) {
                val tile = TileView(this)
                tile.value = 0
                tile.layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    rowSpec = GridLayout.spec(i, 1f)
                    columnSpec = GridLayout.spec(j, 1f)
                }
                tiles[i][j] = tile
                grid.addView(tile)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handleSwipe(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                val dx = event.x - startX
                val dy = event.y - startY
                saveCurrentState()
                when {
                    abs(dx) > abs(dy) && abs(dx) > swipeThreshold -> if (dx > 0) swipeRight() else swipeLeft()
                    abs(dy) > swipeThreshold -> if (dy > 0) swipeDown() else swipeUp()
                }
                return true
            }
        }
        return false
    }

    private fun swipeLeft() {
        if (moveTiles({ line -> mergeTiles(line) })) { addRandomTile(); updateScore() }
    }

    private fun swipeRight() {
        if (moveTiles({ line -> mergeTiles(line.toMutableList().also { it.reverse() }).toMutableList().also { it.reverse() } })) {
            addRandomTile()
            updateScore()
        }
    }

    private fun swipeUp() {
        if (moveTiles({ line -> mergeTiles(line) }, isRow = false, isCol = true)) { addRandomTile(); updateScore() }
    }

    private fun swipeDown() {
        if (moveTiles({ line -> mergeTiles(line.toMutableList().also { it.reverse() }).toMutableList().also { it.reverse() } }, isRow = false, isCol = true)) {
            addRandomTile()
            updateScore()
        }
    }

    private fun moveTiles(mergeFn: (MutableList<Int>) -> List<Int>, isRow: Boolean = true, isCol: Boolean = false): Boolean {
        var moved = false
        for (i in 0..3) {
            val line = if (isCol) (0..3).map { tiles[it][i].value }.toMutableList()
            else if (isRow) tiles[i].map { it.value }.toMutableList()
            else mutableListOf()
            val merged = mergeFn(line)
            for (k in 0..3) {
                val oldValue = if (isCol) tiles[k][i].value else tiles[i][k].value
                val newValue = merged[k]
                if (oldValue != newValue) {
                    if (isCol) tiles[k][i].value = newValue else tiles[i][k].value = newValue
                    moved = true
                }
            }
        }
        return moved
    }

    private fun mergeTiles(line: MutableList<Int>): List<Int> {
        val newLine = line.filter { it != 0 }.toMutableList()
        var i = 0
        while (i < newLine.size - 1) {
            if (newLine[i] == newLine[i + 1]) {
                newLine[i] *= 2
                score += newLine[i]
                newLine.removeAt(i + 1)
            } else {
                i++
            }
        }
        while (newLine.size < 4) newLine.add(0)
        return newLine
    }

    private fun addRandomTile() {
        val empty = mutableListOf<Pair<Int, Int>>()
        for (i in 0..3) for (j in 0..3) if (tiles[i][j].value == 0) empty.add(i to j)
        if (empty.isNotEmpty()) {
            val (i, j) = empty.random()
            tiles[i][j].value = if (Random.nextInt(10) < 9) 2 else 4
        }
    }

    private fun updateScore() {
        binding.scoreText.text = "Очки\n$score"
        if (score > best) {
            best = score
            getSharedPreferences("profile_prefs", MODE_PRIVATE)
                .edit().putInt("game2048_best", best).apply()
        }
        binding.bestText.text = "Рекорд\n$best"
    }

    private fun restartGame() {
        score = 0
        for (i in 0..3) for (j in 0..3) tiles[i][j].value = 0
        addRandomTile()
        addRandomTile()
        updateScore()
    }

    private fun saveCurrentState() {
        for (i in 0..3) for (j in 0..3) lastState[i][j] = tiles[i][j].value
    }

    private fun restorePreviousState() {
        for (i in 0..3) for (j in 0..3) tiles[i][j].value = lastState[i][j]
        updateScore()
    }

    private fun breakRandomTile() {
        val nonEmpty = mutableListOf<Pair<Int, Int>>()
        for (i in 0..3) for (j in 0..3) if (tiles[i][j].value > 0) nonEmpty.add(i to j)
        if (nonEmpty.isNotEmpty()) {
            val (i, j) = nonEmpty.random()
            tiles[i][j].value = 0
        }
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
